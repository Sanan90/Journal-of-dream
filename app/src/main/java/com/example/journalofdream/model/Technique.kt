package com.example.journalofdream.model

// Модель техники осознанных снов (хранится в Firestore)
data class Technique(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val source: String = "",       // опциональный источник (книга, сайт, форум)
    val likes: Int = 0,
    val dislikes: Int = 0,
    val createdAt: Long = 0L       // для сортировки по новизне
)
