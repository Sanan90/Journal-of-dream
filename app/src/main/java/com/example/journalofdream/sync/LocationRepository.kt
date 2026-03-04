package com.example.journalofdream.sync

import android.util.Log
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationRepository(
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
        val locationsRef = remoteDb.collection("users").document(userId).collection("locations")

        listenerRegistration?.remove()
        listenerRegistration = locationsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("LocationRepository", "Firestore listen error", e)
                onSyncError?.invoke("Ошибка синхронизации локаций")
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
                                val remoteLoc = dc.document.toObject(Location::class.java)
                                if (remoteLoc != null && locId != null) {
                                    db.locationDao().insert(remoteLoc.copy(id = locId, ownerUid = userId))
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                if (locId != null) {
                                    db.locationDao().getLocationByIdOnce(locId, userId)?.let {
                                        db.locationDao().delete(it)
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

    suspend fun upsertLocation(location: Location): Result<Unit> {
        val locationDao = db.locationDao()
        val newRowId = locationDao.insert(location)
        val finalLocation = if (location.id == 0) location.copy(id = newRowId.toInt()) else location

        auth.currentUser?.let { user ->
            try {
                remoteDb.collection("users")
                    .document(user.uid)
                    .collection("locations")
                    .document(finalLocation.id.toString())
                    .set(finalLocation.copy(ownerUid = user.uid))
                    .await()
                Log.d("LocationRepository", "upsertLocation: локация [id=${finalLocation.id}] синхронизирована")
            } catch (e: Exception) {
                Log.e("LocationRepository", "upsertLocation: ошибка синхронизации", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteLocation(location: Location): Result<Unit> {
        db.locationDao().delete(location)
        auth.currentUser?.let { user ->
            try {
                remoteDb.collection("users")
                    .document(user.uid)
                    .collection("locations")
                    .document(location.id.toString())
                    .delete()
                    .await()
                Log.d("LocationRepository", "deleteLocation: локация [id=${location.id}] удалена из Firestore")
            } catch (e: Exception) {
                Log.e("LocationRepository", "deleteLocation: ошибка удаления из Firestore", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun migrateGuestLocationsToUser(userUid: String) {
        val locationDao = db.locationDao()
        val guestLocations = locationDao.getLocationsByOwnerOnce("guest")
        for (guestLoc in guestLocations) {
            val updated = guestLoc.copy(ownerUid = userUid)
            locationDao.update(updated)
            try {
                remoteDb.collection("users")
                    .document(userUid)
                    .collection("locations")
                    .document(updated.id.toString())
                    .set(updated.copy(ownerUid = userUid))
                    .await()
            } catch (e: Exception) {
                Log.e("LocationRepository", "migrateGuestLocationsToUser: ошибка загрузки локации", e)
            }
        }
        Log.d("LocationRepository", "migrateGuestLocationsToUser: ${guestLocations.size} локаций мигрировано")
    }
}
