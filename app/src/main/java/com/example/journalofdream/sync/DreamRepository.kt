package com.example.journalofdream.sync

import android.util.Log
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.DreamLocationCrossRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для управления снами и их синхронизации между Room и Firestore.
 * В Firestore документы снов хранятся в коллекции "users/{uid}/dreams".
 * Мы используем localId в качестве ID документа.
 */
class DreamRepository(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val remoteDb: FirebaseFirestore
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Запустить синхронизацию снов из Firestore: слушатель изменений.
     * При вызове, устанавливается SnapshotListener на коллекцию "dreams" текущего пользователя.
     */
    fun startSync() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val dreamsRef = remoteDb.collection("users")
            .document(userId)
            .collection("dreams")

        // Убираем предыдущего слушателя, если был
        listenerRegistration?.remove()
        listenerRegistration = dreamsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("DreamRepository", "Firestore listen error", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (dc in snapshot.documentChanges) {
                    scope.launch {
                        val docIdStr = dc.document.id
                        val dreamId = docIdStr.toIntOrNull()
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                // Преобразуем документ Firestore в объект Dream
                                val remoteDream = dc.document.toObject(Dream::class.java)
                                if (dreamId != null && remoteDream != null) {
                                    // Обновляем данные сна в локальной базе
                                    // Устанавливаем правильный ownerUid и локальный ID
                                    val localDream = remoteDream.copy(
                                        localId = dreamId,
                                        ownerUid = userId,
                                        locationIds = emptyList()  // список локаций для Room не нужен (хранится отдельно)
                                    )
                                    db.dreamDao().insert(localDream)  // OnConflict=REPLACE обновит или добавит запись
                                    // ИСПРАВЛЕНО: обновляем связи сон-локация
                                    db.dreamDao().deleteDreamLocationCrossRefs(dreamId)
                                    for (locId in remoteDream.locationIds) {
                                        // Если локация уже есть локально, привязываем; если нет – создаём заглушку
                                        val locationExists = db.locationDao().getLocationByIdOnce(locId, userId)
                                        if (locationExists != null) {
                                            db.dreamDao().insertDreamLocationCrossRef(DreamLocationCrossRef(dreamId, locId))
                                        } else {
                                            // Создаём временную "пустую" локацию, если она ещё не синхронизирована, чтобы связь не потерять
                                            val placeholderLocation = com.example.journalofdream.model.Location(
                                                id = locId,
                                                ownerUid = userId,
                                                name = "",
                                                description = ""
                                            )
                                            db.locationDao().insert(placeholderLocation)
                                            db.dreamDao().insertDreamLocationCrossRef(DreamLocationCrossRef(dreamId, locId))
                                            Log.d("DreamRepository", "startSync: создана placeholder-локация [id=$locId] для сна [id=$dreamId]")
                                        }
                                    }
                                    Log.d("DreamRepository", "startSync: синхронизирован сон [id=$dreamId] локально")
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                if (dreamId != null) {
                                    // Удаляем сон и связанные записи из локальной базы
                                    db.dreamDao().deleteDreamLocationCrossRefs(dreamId)
                                    db.dreamDao().getDreamByIdOnce(dreamId, userId)?.let { localDream ->
                                        db.dreamDao().delete(localDream)
                                    }
                                    Log.d("DreamRepository", "startSync: удален сон [id=$dreamId] из локальной базы")
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d("DreamRepository", "startSync: listener attached for user=$userId")
    }

    /**
     * Остановить синхронизацию снов (удалить слушатель Firestore).
     */
    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        Log.d("DreamRepository", "stopSync: listener removed")
    }

    /**
     * Локально сохранить/обновить сон и привязанные локации, а также синхронизировать это с Firestore.
     * @param dream объект сна (поле ownerUid должно быть задано текущим пользователем или "guest")
     * @param locationIds список идентификаторов локаций, связанных со сном
     */
    suspend fun upsertDream(dream: Dream, locationIds: List<Int>) {
        val dreamDao = db.dreamDao()
        var newDream = dream
        // Если у сна нет локального ID (новый сон), вставляем и получаем сгенерированный ID
        if (newDream.localId == 0) {
            val newId = dreamDao.insert(newDream)
            newDream = newDream.copy(localId = newId.toInt())
        } else {
            // Если localId уже задан, обновляем существующую запись (OnConflict=REPLACE)
            dreamDao.insert(newDream)
        }
        // ИСПРАВЛЕНО: обновляем таблицу связей. Сначала удаляем старые привязки сна к локациям, затем добавляем актуальные.
        dreamDao.deleteDreamLocationCrossRefs(newDream.localId)
        for (locId in locationIds) {
            dreamDao.insertDreamLocationCrossRef(DreamLocationCrossRef(newDream.localId, locId))
        }
        // Синхронизируем изменения с Firestore, если пользователь авторизован
        auth.currentUser?.uid?.let { userId ->
            val firestoreDream = newDream.copy(ownerUid = userId, locationIds = locationIds)
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(newDream.localId.toString())
                .set(firestoreDream)
                .await()
            Log.d("DreamRepository", "upsertDream: сон [id=${newDream.localId}] отправлен/обновлен в Firestore")
        }
    }

    /**
     * Удаляет сон из локальной базы и Firestore (если пользователь авторизован).
     * Предполагается, что локальные связи (DreamLocationCrossRef) уже удалены перед вызовом.
     */
    suspend fun deleteDream(dream: Dream) {
        // ИСПРАВЛЕНО: перед удалением сна удаляем его связи (перестраховка, хотя CASCADE сделает то же самое)
        db.dreamDao().deleteDreamLocationCrossRefs(dream.localId)
        db.dreamDao().delete(dream)
        auth.currentUser?.uid?.let { userId ->
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(dream.localId.toString())
                .delete()
                .await()
            Log.d("DreamRepository", "deleteDream: сон [id=${dream.localId}] удален из Firestore")
        }
    }

    /**
     * Мигрирует все сны, созданные в гостевом режиме, в учетную запись пользователя при логине.
     * Обновляет поле ownerUid с "guest" на указанный userUid, сохраняет изменения локально и отправляет сны в Firestore.
     * (Предполагается, что связанные локации уже мигрированы LocationRepository.)
     */
    suspend fun migrateGuestRecordsToUser(userUid: String) {
        val dreamDao = db.dreamDao()
        val guestDreams = dreamDao.getDreamsByOwnerOnce("guest")
        for (guestDream in guestDreams) {
            // Обновляем ownerUid на UID залогиненного пользователя
            val migratedDream = guestDream.copy(ownerUid = userUid)
            // ИСПРАВЛЕНО: используем вставку с REPLACE (или update) для переноса сна под новым владельцем
            // При REPLACE старая запись (guest) будет удалена, а новая добавлена
            dreamDao.insert(migratedDream)
            // Получаем все ID локаций, связанных с гостевым сном
            val locationIds = dreamDao.getLocationIdsForDream(guestDream.localId)
            // Перезаписываем связи сон-локация для нового владельца (после REPLACE старые связи могли удалиться каскадом)
            dreamDao.deleteDreamLocationCrossRefs(migratedDream.localId)
            for (locId in locationIds) {
                dreamDao.insertDreamLocationCrossRef(DreamLocationCrossRef(migratedDream.localId, locId))
            }
            // Загружаем сон в Firestore под новым пользователем
            remoteDb.collection("users")
                .document(userUid)
                .collection("dreams")
                .document(migratedDream.localId.toString())
                .set(migratedDream.copy(ownerUid = userUid, locationIds = locationIds))
                .await()
        }
        Log.d("DreamRepository", "migrateGuestRecordsToUser: мигрировано снов: ${guestDreams.size} для пользователя $userUid")
    }
}
