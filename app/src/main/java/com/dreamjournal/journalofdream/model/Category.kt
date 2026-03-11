package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isCustom: Boolean,
    val ownerUid: String = "default",
    val color: String = "#9C27B0" // hex цвет, по умолчанию фиолетовый
)
