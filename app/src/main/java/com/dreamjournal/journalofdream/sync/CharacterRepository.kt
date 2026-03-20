package com.dreamjournal.journalofdream.sync

import android.util.Log
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.DreamCharacter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CharacterRepository(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val remoteDb: FirebaseFirestore
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    var onSyncError: ((String) -> Unit)? = null

    /**
     * Важно: не используем INSERT ... REPLACE для уже существующего персонажа.
     * REPLACE удаляет старую строку и вставляет новую заново,
     * а из-за CASCADE это сносит связи в dream_character_cross_ref.
     */
    private suspend fun upsertCharacterLocallyPreservingRefs(character: DreamCharacter) {
        val existing = db.characterDao().getCharacterByIdOnce(character.id, character.ownerUid)
        if (existing == null) {
            db.characterDao().insert(character)
        } else {
            db.characterDao().update(character)
        }
    }

    fun startSync() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val ref = remoteDb.collection("users").document(userId).collection("characters")

        listenerRegistration?.remove()
        listenerRegistration = ref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("CharacterRepository", "Firestore listen error", e)
                onSyncError?.invoke("Ошибка синхронизации персонажей")
                return@addSnapshotListener
            }
            snapshot?.documentChanges?.forEach { dc ->
                scope.launch {
                    val charId = dc.document.id.toIntOrNull()
                    when (dc.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val remoteCharacter = dc.document.toObject(DreamCharacter::class.java)
                            if (charId != null && remoteCharacter != null) {
                                upsertCharacterLocallyPreservingRefs(
                                    remoteCharacter.copy(id = charId, ownerUid = userId)
                                )
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            if (charId != null) {
                                db.characterDao().getCharacterByIdOnce(charId, userId)?.let { db.characterDao().delete(it) }
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

    suspend fun upsertCharacter(character: DreamCharacter): Result<Unit> {
        val dao = db.characterDao()
        val finalCharacter = if (character.id == 0) {
            val newRowId = dao.insert(character)
            character.copy(id = newRowId.toInt())
        } else {
            upsertCharacterLocallyPreservingRefs(character)
            character
        }

        auth.currentUser?.let { user ->
            try {
                remoteDb.collection("users").document(user.uid).collection("characters")
                    .document(finalCharacter.id.toString())
                    .set(finalCharacter.copy(ownerUid = user.uid))
                    .await()
            } catch (e: Exception) {
                Log.e("CharacterRepository", "upsertCharacter sync error", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteCharacter(character: DreamCharacter): Result<Unit> {
        db.characterDao().delete(character)
        auth.currentUser?.let { user ->
            try {
                val userRef = remoteDb.collection("users").document(user.uid)
                userRef.collection("characters").document(character.id.toString()).delete().await()
                val dreamsWithCharacter = userRef.collection("dreams")
                    .whereArrayContains("characterIds", character.id)
                    .get().await()
                for (dreamDoc in dreamsWithCharacter.documents) {
                    dreamDoc.reference.update("characterIds", FieldValue.arrayRemove(character.id)).await()
                }
            } catch (e: Exception) {
                Log.e("CharacterRepository", "deleteCharacter sync error", e)
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun migrateGuestCharactersToUser(userUid: String) {
        val dao = db.characterDao()
        val guestCharacters = dao.getCharactersByOwnerOnce("guest")
        for (guest in guestCharacters) {
            val updated = guest.copy(ownerUid = userUid)
            dao.update(updated)
            try {
                remoteDb.collection("users").document(userUid).collection("characters")
                    .document(updated.id.toString())
                    .set(updated.copy(ownerUid = userUid))
                    .await()
            } catch (e: Exception) {
                Log.e("CharacterRepository", "migrateGuestCharactersToUser sync error", e)
            }
        }
    }
}
