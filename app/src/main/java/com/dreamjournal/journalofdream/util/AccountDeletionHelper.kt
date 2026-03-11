package com.dreamjournal.journalofdream.util

import android.content.Context
import android.util.Log
import com.dreamjournal.journalofdream.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.dreamjournal.journalofdream.R

private const val DELETE_TAG = "AccountDeletion"

suspend fun deleteCurrentUserAccountAndData(context: Context): Result<Unit> {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser ?: return Result.failure(Exception("Пользователь не авторизован"))
    val uid = user.uid

    return try {
        // 1. Удаляем публичную активность пользователя best effort:
        // лайки/дизлайки удаляем, комментарии анонимизируем
        tryCleanupPublicActivityBestEffort(firestore, uid)

        // 2. Удаляем личные данные пользователя из Firestore
        deleteUserSubcollection(firestore, uid, "dreams")
        deleteUserSubcollection(firestore, uid, "locations")
        deleteUserSubcollection(firestore, uid, "settings")

        firestore.collection("users")
            .document(uid)
            .delete()
            .await()

        // 3. Удаляем локальные данные из Room
        val db = AppDatabase.getInstance(context)
        db.dreamDao().deleteAllDreamsByOwner(uid)
        db.locationDao().deleteAllLocationsByOwner(uid)
        db.categoryDao().deleteCustomCategoriesForUser(uid)

        // 4. Чистим локальные security prefs
        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("pin_hash_cache")
            .remove("pin_hash")
            .putBoolean("pin_enabled", false)
            .apply()

        // 5. Удаляем Firebase Auth аккаунт
        user.delete().await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(DELETE_TAG, "Ошибка полного удаления аккаунта", e)
        Result.failure(e)
    }
}

private suspend fun deleteUserSubcollection(
    firestore: FirebaseFirestore,
    uid: String,
    subcollection: String
) {
    val docs = firestore.collection("users")
        .document(uid)
        .collection(subcollection)
        .get()
        .await()

    for (doc in docs.documents) {
        doc.reference.delete().await()
    }
}

private suspend fun tryCleanupPublicActivityBestEffort(
    firestore: FirebaseFirestore,
    uid: String
) {
    try {
        val techniques = firestore.collection("techniques").get().await()

        for (techDoc in techniques.documents) {
            val techniqueId = techDoc.id
            tryDeleteVoteForTechnique(firestore, techniqueId, uid)
            tryAnonymizeOwnCommentsForTechnique(firestore, techniqueId, uid)
        }
    } catch (e: Exception) {
        Log.w(DELETE_TAG, "Не удалось полностью очистить публичную активность", e)
    }
}

private suspend fun tryDeleteVoteForTechnique(
    firestore: FirebaseFirestore,
    techniqueId: String,
    uid: String
) {
    try {
        val techniqueRef = firestore.collection("techniques").document(techniqueId)
        val voteRef = techniqueRef.collection("votes").document(uid)
        val voteSnap = voteRef.get().await()

        if (!voteSnap.exists()) return

        val vote = voteSnap.getString("vote")
        firestore.runTransaction { transaction ->
            val techSnap = transaction.get(techniqueRef)
            var likes = techSnap.getLong("likes")?.toInt() ?: 0
            var dislikes = techSnap.getLong("dislikes")?.toInt() ?: 0

            when (vote) {
                "like" -> likes = maxOf(0, likes - 1)
                "dislike" -> dislikes = maxOf(0, dislikes - 1)
            }

            transaction.delete(voteRef)
            transaction.update(
                techniqueRef,
                mapOf(
                    "likes" to likes,
                    "dislikes" to dislikes
                )
            )
        }.await()
    } catch (e: Exception) {
        Log.w(DELETE_TAG, "Не удалось удалить vote для techniqueId=$techniqueId", e)
    }
}

private suspend fun tryAnonymizeOwnCommentsForTechnique(
    firestore: FirebaseFirestore,
    techniqueId: String,
    uid: String
) {
    try {
        val techniqueRef = firestore.collection("techniques").document(techniqueId)
        val commentsRef = techniqueRef.collection("comments")
        val commentsSnapshot = commentsRef.get().await()

        for (commentDoc in commentsSnapshot.documents) {
            val authorUid = commentDoc.getString("authorUid")
            val uidField = commentDoc.getString("uid")
            val userIdField = commentDoc.getString("userId")
            val authorIdField = commentDoc.getString("authorId")

            val belongsToCurrentUser =
                authorUid == uid ||
                        uidField == uid ||
                        userIdField == uid ||
                        authorIdField == uid

            if (belongsToCurrentUser) {
                Log.d(DELETE_TAG, "Анонимизируем только comment=${commentDoc.id} у techniqueId=$techniqueId")
                tryAnonymizeSingleComment(commentDoc)
            }
        }
    } catch (e: Exception) {
        Log.w(DELETE_TAG, "Не удалось анонимизировать comments для techniqueId=$techniqueId", e)
    }
}

private suspend fun tryAnonymizeSingleComment(
    commentDoc: com.google.firebase.firestore.DocumentSnapshot
) {
    try {
        val commentRef = commentDoc.reference

        val updates = hashMapOf<String, Any?>(
            "authorUid" to "__deleted_user__",
            "uid" to "__deleted_user__",
            "userId" to "__deleted_user__",
            "authorId" to "__deleted_user__",

            "email" to "",
            "authorEmail" to "",
            "authorName" to "",
            "username" to "",
            "displayName" to "",
            "userDisplayName" to "",
            "name" to "",

            "avatarUrl" to "",
            "photoUrl" to "",
            "userPhotoUrl" to "",

            "isDeletedUser" to true,
            "deletedAt" to FieldValue.serverTimestamp()
        )

        commentRef.update(updates).await()
    } catch (e: Exception) {
        Log.w(DELETE_TAG, "Не удалось анонимизировать comment ${commentDoc.id}", e)
    }
}