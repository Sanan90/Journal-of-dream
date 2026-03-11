package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Локация. Первичный ключ – id.
 * ownerUid указывает владельца ("guest" или UID пользователя).
 */
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,         // PK для Location (автоинкремент)
    var ownerUid: String = "guest",
    var name: String = "",
    var description: String = ""
)
