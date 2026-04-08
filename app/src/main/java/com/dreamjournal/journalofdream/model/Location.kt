package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Локация. Первичный ключ – id.
 * ownerUid указывает владельца ("guest" или UID пользователя).
 * backgroundId — ID выбранного фона из DreamBackgrounds (0 = без фона).
 */
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var ownerUid: String = "guest",
    var name: String = "",
    var description: String = "",
    var backgroundId: Int = 0   // ID фона из DreamBackgrounds (0 = без фона)
)
