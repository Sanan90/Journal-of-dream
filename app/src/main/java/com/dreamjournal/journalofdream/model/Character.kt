package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class DreamCharacter(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var ownerUid: String = "guest",
    var name: String = "",
    var description: String = ""
)
