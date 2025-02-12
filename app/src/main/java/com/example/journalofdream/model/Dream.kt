// Файл: com/example/journalofdream/model/Dream.kt

package com.example.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "dreams")
data class Dream(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val category: String = "Без категории"
)
