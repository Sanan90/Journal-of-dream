package com.example.journalofdream.sync

import android.util.Log
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.database.LocationDao
import com.example.journalofdream.model.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для синхронизации локаций между локальной БД и Firestore.
 * Firestore: коллекция "users/{uid}/locations".
 */
class LocationRepository(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val remoteDb: FirebaseFirestore
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Запустить слушатель Firestore на коллекцию "locations" текущего пользователя.
     * Любые изменения (добавление/обновление/удаление) синхронно отражаются в локальной базе данных.
     */
    fun startSync() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val locationsRef = remoteDb.collection("users")
            .document(userId)
            .collection("locations")

        // Удаляем предыдущего слушателя, если он уже был запущен
        listenerRegistration?.remove()
        listenerRegistration = locationsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("LocationRepository", "Firestore listen error", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (dc in snapshot.documentChanges) {
                    scope.launch {
                        val docId = dc.document.id
                        val locId = docId.toIntOrNull()
                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                // Преобразуем документ Firestore в объект Location
                                val remoteLoc = dc.document.toObject(Location::class.java)
                                if (remoteLoc != null && locId != null) {
                                    // Устанавливаем корректный ownerUid текущего пользователя и локальный ID, затем сохраняем в Room
                                    val localLocation = remoteLoc.copy(id = locId, ownerUid = userId)
                                    db.locationDao().insert(localLocation)  // OnConflict=REPLACE
                                    Log.d("LocationRepository", "Sync: локация [id=$locId] добавлена/обновлена локально.")
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                if (locId != null) {
                                    // Находим локацию в локальной базе по ID и удаляем её
                                    db.locationDao().getLocationByIdOnce(locId, userId)?.let { localLoc ->
                                        // При удалении локации сработает CASCADE на связи (dream_location_cross_ref), и связанные сны потеряют эту связь.
                                        db.locationDao().delete(localLoc)
                                        Log.d("LocationRepository", "Sync: локация [id=$locId] удалена локально.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d("LocationRepository", "startSync: listener attached for user=$userId")
    }

    /**
     * Остановить слушатель синхронизации локаций (например, при выходе из аккаунта).
     */
    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        Log.d("LocationRepository", "stopSync: listener removed")
    }

    /**
     * Добавить или обновить локацию в локальной БД и (если авторизован) в Firestore.
     */
    suspend fun upsertLocation(location: Location) {
        val locationDao = db.locationDao()
        // Сохраняем локацию в локальной базе данных
        val newRowId = locationDao.insert(location)
        var finalLocation = location
        if (location.id == 0) {
            // Если это новая локация (id ещё не сгенерирован), обновляем id из возвращённого значения
            finalLocation = location.copy(id = newRowId.toInt())
        }
        // Если пользователь авторизован, отправляем обновление в Firestore
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            remoteDb.collection("users")
                .document(userId)
                .collection("locations")
                .document(finalLocation.id.toString())
                .set(finalLocation.copy(ownerUid = userId))
                .await()
            Log.d("LocationRepository", "upsertLocation: локация [id=${finalLocation.id}] отправлена в Firestore")
        }
    }

    /**
     * Удаляем локацию из локальной базы (и из Firestore, если нужно).
     * При локальном удалении сработает каскад: все связи с этой локацией в DreamLocationCrossRef будут удалены.
     */
    suspend fun deleteLocation(location: Location) {
        db.locationDao().delete(location)
        val user = auth.currentUser
        if (user != null) {
            remoteDb.collection("users")
                .document(user.uid)
                .collection("locations")
                .document(location.id.toString())
                .delete()
                .await()
            Log.d("LocationRepository", "deleteLocation: локация [id=${location.id}] удалена из Firestore")
        }
    }

    /**
     * Миграция гостевых локаций текущего устройства в профиль пользователя при логине.
     * Все локации с ownerUid = "guest" получают нового ownerUid (userUid) и отправляются в Firestore.
     */
    suspend fun migrateGuestLocationsToUser(userUid: String) {
        val locationDao = db.locationDao()
        val guestLocations = locationDao.getLocationsByOwnerOnce("guest")
        for (guestLoc in guestLocations) {
            val updated = guestLoc.copy(ownerUid = userUid)
            // ИСПРАВЛЕНО: используем обновление, а не REPLACE-вставку, чтобы не удалить запись и не потерять связи с снами
            locationDao.update(updated)
            // Сохраняем локацию в Firestore под новым пользователем
            remoteDb.collection("users")
                .document(userUid)
                .collection("locations")
                .document(updated.id.toString())
                .set(updated.copy(ownerUid = userUid))
                .await()
        }
        Log.d("LocationRepository", "migrateGuestLocationsToUser: ${guestLocations.size} локаций перенесено в профиль пользователя.")
    }
}
