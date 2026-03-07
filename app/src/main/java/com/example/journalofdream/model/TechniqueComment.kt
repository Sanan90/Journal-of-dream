package com.example.journalofdream.model

// Комментарий к технике (хранится в Firestore)
data class TechniqueComment(
    val id: String = "",
    val techniqueId: String = "",
    val authorUid: String = "",
    val authorName: String = "",  // имя/email для отображения
    val text: String = "",
    val likes: Int = 0,
    val createdAt: Long = 0L
)
