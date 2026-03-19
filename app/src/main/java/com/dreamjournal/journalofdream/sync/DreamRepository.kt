package com.dreamjournal.journalofdream.sync

import android.util.Log
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.DreamCharacterCrossRef
import com.dreamjournal.journalofdream.model.DreamLocationCrossRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DreamRepository(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val remoteDb: FirebaseFirestore
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Callback для передачи ошибок во ViewModel
    var onSyncError: ((String) -> Unit)? = null

    fun startSync() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val dreamsRef = remoteDb.collection("users").document(userId).collection("dreams")

        listenerRegistration?.remove()
        listenerRegistration = dreamsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("DreamRepository", "Firestore listen error", e)
                onSyncError?.invoke("Ошибка синхронизации снов")
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
                                val remoteDream = dc.document.toObject(Dream::class.java)
                                if (dreamId != null && remoteDream != null) {
                                    val localDream = remoteDream.copy(
                                        localId = dreamId,
                                        ownerUid = userId,
                                        locationIds = emptyList(),
                                        characterIds = emptyList()
                                    )
                                    db.dreamDao().insert(localDream)
                                    db.dreamDao().deleteDreamLocationCrossRefs(dreamId)
                                    db.dreamDao().deleteDreamCharacterCrossRefs(dreamId)
                                    for (locId in remoteDream.locationIds) {
                                        // Привязываем только если локация реально существует.
                                        // Если локация удалена — пропускаем, чтобы не создавать призрак без названия.
                                        val locationExists = db.locationDao().getLocationByIdOnce(locId, userId)
                                        if (locationExists != null) {
                                            db.dreamDao().insertDreamLocationCrossRef(DreamLocationCrossRef(dreamId, locId))
                                        }
                                    }
                                    for (characterId in remoteDream.characterIds) {
                                        val characterExists = db.characterDao().getCharacterByIdOnce(characterId, userId)
                                        if (characterExists != null) {
                                            db.dreamDao().insertDreamCharacterCrossRef(DreamCharacterCrossRef(dreamId, characterId))
                                        }
                                    }
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                if (dreamId != null) {
                                    db.dreamDao().deleteDreamLocationCrossRefs(dreamId)
                                    db.dreamDao().deleteDreamCharacterCrossRefs(dreamId)
                                    db.dreamDao().getDreamByIdOnce(dreamId, userId)?.let { localDream ->
                                        db.dreamDao().delete(localDream)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    suspend fun upsertDream(dream: Dream, locationIds: List<Int>, characterIds: List<Int>): Result<Unit> {
        val dreamDao = db.dreamDao()
        var newDream = dream
        if (newDream.localId == 0) {
            val newId = dreamDao.insert(newDream)
            newDream = newDream.copy(localId = newId.toInt())
        } else {
            dreamDao.insert(newDream)
        }
        dreamDao.deleteDreamLocationCrossRefs(newDream.localId)
        for (locId in locationIds) {
            dreamDao.insertDreamLocationCrossRef(DreamLocationCrossRef(newDream.localId, locId))
        }
        dreamDao.deleteDreamCharacterCrossRefs(newDream.localId)
        for (characterId in characterIds) {
            dreamDao.insertDreamCharacterCrossRef(DreamCharacterCrossRef(newDream.localId, characterId))
        }
        // Синхронизация с Firestore
        auth.currentUser?.uid?.let { userId ->
            try {
                val firestoreDream = newDream.copy(ownerUid = userId, locationIds = locationIds, characterIds = characterIds)
                remoteDb.collection("users")
                    .document(userId)
                    .collection("dreams")
                    .document(newDream.localId.toString())
                    .set(firestoreDream)
                    .await()
                Log.d("DreamRepository", "upsertDream: сон [id=${newDream.localId}] синхронизирован")
            } catch (e: Exception) {
                Log.e("DreamRepository", "upsertDream: ошибка синхронизации", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteDream(dream: Dream): Result<Unit> {
        db.dreamDao().deleteDreamLocationCrossRefs(dream.localId)
        db.dreamDao().deleteDreamCharacterCrossRefs(dream.localId)
        db.dreamDao().delete(dream)
        auth.currentUser?.uid?.let { userId ->
            try {
                remoteDb.collection("users")
                    .document(userId)
                    .collection("dreams")
                    .document(dream.localId.toString())
                    .delete()
                    .await()
                Log.d("DreamRepository", "deleteDream: сон [id=${dream.localId}] удалён из Firestore")
            } catch (e: Exception) {
                Log.e("DreamRepository", "deleteDream: ошибка удаления из Firestore", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun migrateGuestRecordsToUser(userUid: String) {
        val dreamDao = db.dreamDao()
        val guestDreams = dreamDao.getDreamsByOwnerOnce("guest")
        for (guestDream in guestDreams) {
            val migratedDream = guestDream.copy(ownerUid = userUid)
            dreamDao.insert(migratedDream)
            val locationIds = dreamDao.getLocationIdsForDream(guestDream.localId)
            val characterIds = dreamDao.getCharacterIdsForDream(guestDream.localId)
            dreamDao.deleteDreamLocationCrossRefs(migratedDream.localId)
            dreamDao.deleteDreamCharacterCrossRefs(migratedDream.localId)
            for (locId in locationIds) {
                dreamDao.insertDreamLocationCrossRef(DreamLocationCrossRef(migratedDream.localId, locId))
            }
            for (characterId in characterIds) {
                dreamDao.insertDreamCharacterCrossRef(DreamCharacterCrossRef(migratedDream.localId, characterId))
            }
            try {
                remoteDb.collection("users")
                    .document(userUid)
                    .collection("dreams")
                    .document(migratedDream.localId.toString())
                    .set(migratedDream.copy(ownerUid = userUid, locationIds = locationIds, characterIds = characterIds))
                    .await()
            } catch (e: Exception) {
                Log.e("DreamRepository", "migrateGuestRecordsToUser: ошибка загрузки сна", e)
            }
        }
        Log.d("DreamRepository", "migrateGuestRecordsToUser: мигрировано ${guestDreams.size} снов")
    }
}
