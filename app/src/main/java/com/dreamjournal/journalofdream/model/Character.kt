package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Образ (персонаж) сна.
 * backgroundId — ID выбранного фона из DreamBackgrounds (0 = без фона).
 */
@Entity(tableName = "characters")
data class DreamCharacter(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var ownerUid: String = "guest",
    var name: String = "",
    var description: String = "",
    var backgroundId: Int = 0   // ID фона из DreamBackgrounds (0 = без фона)
)
