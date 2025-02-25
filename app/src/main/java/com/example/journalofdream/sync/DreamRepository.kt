package com.example.journalofdream.sync

import android.util.Log
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Dream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * Repository (Room <-> Firestore).
 */
class DreamRepository(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val remoteDb: FirebaseFirestore
) {
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Запустить синхронизацию, если пользователь авторизован.
     * Ставим snapshotListener на коллекцию `dreams` в Firestore -> локальная база (merge).
     */
    fun startSync() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        val collectionRef = remoteDb.collection("users")
            .document(userId)
            .collection("dreams")

        // Если уже слушали - удаляем старый листенер
        listenerRegistration?.remove()
        listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("DreamRepository", "Firestore listen error", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Обрабатываем изменения построчно
                for (dc in snapshot.documentChanges) {
                    scope.launch {
                        val docId = dc.document.id
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val dream = dc.document.toObject(Dream::class.java)
                                    ?.copy(id = docId)
                                if (dream != null) {
                                    // Обновляем/вставляем локально
                                    // меняем ownerUid на userId (чтобы было согласовано)
                                    val finalDream = dream.copy(ownerUid = userId)
                                    db.dreamDao().insert(finalDream)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                // Удаляем локально
                                val existing = db.dreamDao().getDreamByIdOnce(docId)
                                if (existing != null) {
                                    db.dreamDao().delete(existing)
                                }
                            }
                        }
                    }
                }
            }
        }

        Log.d("DreamRepository", "startSync: snapshotListener for user=$userId started.")
    }

    /**
     * Отключить слушатель Firestore
     */
    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        Log.d("DreamRepository", "stopSync: snapshotListener removed.")
    }

    /**
     * Добавить/обновить сон локально и отправить в Firestore
     */
    suspend fun upsertDream(dream: Dream) {
        val dao = db.dreamDao()
        dao.insert(dream)  // локально

        val user = auth.currentUser
        // Если авторизован, пушим в Firestore
        if (user != null) {
            val userId = user.uid
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(dream.id)
                .set(dream.copy(ownerUid = userId))
                .await()
        }
    }

    /**
     * Удалить сон локально и (если авторизован) в Firestore
     */
    suspend fun deleteDream(dream: Dream) {
        db.dreamDao().delete(dream)

        val user = auth.currentUser
        if (user != null) {
            remoteDb.collection("users")
                .document(user.uid)
                .collection("dreams")
                .document(dream.id)
                .delete()
                .await()
        }
    }

    /**
     * Собрать все локальные записи ownerUid="guest" -> поменять на userUid -> пушить в Firestore
     */
    suspend fun migrateGuestRecordsToUser(userUid: String) {
        val dao = db.dreamDao()
        val guestDreams = dao.getDreamsByOwnerOnce("guest")
        for (g in guestDreams) {
            val updated = g.copy(ownerUid = userUid)
            dao.insert(updated) // обновляем локально
            // пушим
            remoteDb.collection("users")
                .document(userUid)
                .collection("dreams")
                .document(updated.id)
                .set(updated)
                .await()
        }
    }
}
