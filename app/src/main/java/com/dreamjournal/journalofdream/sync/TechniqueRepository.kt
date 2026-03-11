package com.dreamjournal.journalofdream.sync

import android.util.Log
import com.dreamjournal.journalofdream.model.Technique
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class TechniqueRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("techniques")
    private var listener: ListenerRegistration? = null

    // Слушаем техники в реальном времени
    fun startListening(onUpdate: (List<Technique>) -> Unit) {
        listener?.remove()
        listener = col.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("TechniqueRepo", "Ошибка загрузки техник", e)
                return@addSnapshotListener
            }
            val docs = snapshot?.documents ?: return@addSnapshotListener
            val list = docs.mapNotNull { doc ->
                doc.toObject(Technique::class.java)?.copy(id = doc.id)
            }
            // Для каждой техники слушаем подколлекцию comments и обновляем счётчик
            docs.forEach { doc ->
                doc.reference.collection("comments")
                    .addSnapshotListener { commentsSnap, _ ->
                        val count = commentsSnap?.size() ?: 0
                        // Обновляем commentsCount в Firestore только если не совпадает
                        val current = doc.getLong("commentsCount")?.toInt() ?: 0
                        if (current != count) {
                            doc.reference.update("commentsCount", count)
                        }
                    }
            }
            onUpdate(list)
        }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    // Добавить технику (только админ)
    suspend fun addTechnique(technique: Technique): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to technique.name,
                "description" to technique.description,
                "source" to technique.source,
                "likes" to 0,
                "dislikes" to 0,
                "createdAt" to System.currentTimeMillis()
            )
            col.add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TechniqueRepo", "Ошибка добавления техники", e)
            Result.failure(e)
        }
    }

    // Обновить технику (только админ)
    suspend fun updateTechnique(technique: Technique): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to technique.name,
                "description" to technique.description,
                "source" to technique.source,
                "likes" to technique.likes,
                "dislikes" to technique.dislikes,
                "createdAt" to technique.createdAt
            )
            col.document(technique.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TechniqueRepo", "Ошибка обновления техники", e)
            Result.failure(e)
        }
    }

    // Удалить технику (только админ)
    suspend fun deleteTechnique(id: String): Result<Unit> {
        return try {
            col.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TechniqueRepo", "Ошибка удаления техники", e)
            Result.failure(e)
        }
    }

    // Получить голос текущего пользователя для техники
    suspend fun getUserVote(techniqueId: String): String? {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return null
        return try {
            val doc = col.document(techniqueId)
                .collection("votes")
                .document(uid)
                .get()
                .await()
            doc.getString("vote") // "like", "dislike" или null
        } catch (e: Exception) { null }
    }

    // Загрузить все голоса текущего пользователя за один запрос
    suspend fun getAllUserVotes(): Map<String, String> {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyMap()
        return try {
            // Получаем все документы голосов из группы коллекций votes где uid совпадает
            val result = mutableMapOf<String, String>()
            val techniques = col.get().await()
            techniques.documents.forEach { techDoc ->
                val voteDoc = col.document(techDoc.id)
                    .collection("votes")
                    .document(uid)
                    .get()
                    .await()
                val vote = voteDoc.getString("vote")
                if (vote != null) result[techDoc.id] = vote
            }
            result
        } catch (e: Exception) { emptyMap() }
    }

    // Поставить лайк или дизлайк (атомарно, голос в Firestore под uid)
    suspend fun vote(techniqueId: String, isLike: Boolean, previousVote: String?): Result<Unit> {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Не авторизован"))
        return try {
            val newVote = if (isLike) "like" else "dislike"
            val voteRef = col.document(techniqueId).collection("votes").document(uid)
            val techniqueRef = col.document(techniqueId)

            db.runTransaction { transaction ->
                val techSnap = transaction.get(techniqueRef)
                var likes = techSnap.getLong("likes")?.toInt() ?: 0
                var dislikes = techSnap.getLong("dislikes")?.toInt() ?: 0

                // Отменяем предыдущий голос
                when (previousVote) {
                    "like" -> likes = maxOf(0, likes - 1)
                    "dislike" -> dislikes = maxOf(0, dislikes - 1)
                }

                if (previousVote == newVote) {
                    // Повторное нажатие — снимаем голос
                    transaction.delete(voteRef)
                } else {
                    // Новый голос
                    if (isLike) likes++ else dislikes++
                    transaction.set(voteRef, mapOf("vote" to newVote))
                }

                transaction.update(techniqueRef, mapOf("likes" to likes, "dislikes" to dislikes))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TechniqueRepo", "Ошибка голосования", e)
            Result.failure(e)
        }
    }
}
