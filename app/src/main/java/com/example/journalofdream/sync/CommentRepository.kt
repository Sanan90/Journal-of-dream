package com.example.journalofdream.sync

import android.util.Log
import com.example.journalofdream.model.TechniqueComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CommentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null

    // Слушаем комментарии для конкретной техники
    fun startListening(techniqueId: String, onUpdate: (List<TechniqueComment>) -> Unit) {
        listener?.remove()
        listener = db.collection("techniques")
            .document(techniqueId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("CommentRepo", "Ошибка", e); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TechniqueComment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(list)
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    // Добавить комментарий
    suspend fun addComment(techniqueId: String, text: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Не авторизован"))
        val name = user.displayName?.ifBlank { null }
            ?: user.email?.substringBefore("@")
            ?: "Аноним"
        return try {
            val techniqueRef = db.collection("techniques").document(techniqueId)
            val commentsRef = techniqueRef.collection("comments")
            db.runTransaction { transaction ->
                val techSnap = transaction.get(techniqueRef)
                val count = techSnap.getLong("commentsCount")?.toInt() ?: 0
                val newDoc = commentsRef.document()
                transaction.set(newDoc, mapOf(
                    "techniqueId" to techniqueId,
                    "authorUid" to user.uid,
                    "authorName" to name,
                    "text" to text.trim(),
                    "likes" to 0,
                    "createdAt" to System.currentTimeMillis()
                ))
                transaction.update(techniqueRef, "commentsCount", count + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CommentRepo", "Ошибка добавления", e)
            Result.failure(e)
        }
    }

    // Лайк комментария (атомарно)
    suspend fun likeComment(techniqueId: String, commentId: String, isLiked: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Не авторизован"))
        return try {
            val commentRef = db.collection("techniques")
                .document(techniqueId)
                .collection("comments")
                .document(commentId)
            val likeRef = commentRef.collection("likes").document(uid)

            db.runTransaction { transaction ->
                val snap = transaction.get(commentRef)
                var likes = snap.getLong("likes")?.toInt() ?: 0
                if (isLiked) {
                    likes = maxOf(0, likes - 1)
                    transaction.delete(likeRef)
                } else {
                    likes++
                    transaction.set(likeRef, mapOf("uid" to uid))
                }
                transaction.update(commentRef, "likes", likes)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CommentRepo", "Ошибка лайка", e)
            Result.failure(e)
        }
    }

    // Получить лайкнутые комментарии текущего пользователя
    suspend fun getLikedCommentIds(techniqueId: String): Set<String> {
        val uid = auth.currentUser?.uid ?: return emptySet()
        return try {
            val comments = db.collection("techniques")
                .document(techniqueId)
                .collection("comments")
                .get().await()
            val liked = mutableSetOf<String>()
            comments.documents.forEach { doc ->
                val likeDoc = doc.reference.collection("likes").document(uid).get().await()
                if (likeDoc.exists()) liked.add(doc.id)
            }
            liked
        } catch (e: Exception) { emptySet() }
    }

    // Удалить комментарий
    suspend fun deleteComment(techniqueId: String, commentId: String): Result<Unit> {
        return try {
            val techniqueRef = db.collection("techniques").document(techniqueId)
            val commentRef = techniqueRef.collection("comments").document(commentId)
            db.runTransaction { transaction ->
                val techSnap = transaction.get(techniqueRef)
                val count = techSnap.getLong("commentsCount")?.toInt() ?: 0
                transaction.delete(commentRef)
                transaction.update(techniqueRef, "commentsCount", maxOf(0, count - 1))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
