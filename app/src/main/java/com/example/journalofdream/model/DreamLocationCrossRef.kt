// Файл: com/example/journalofdream/model/DreamLocationCrossRef.kt

package com.example.journalofdream.model

import androidx.room.Entity

@Entity(primaryKeys = ["dreamId", "locationId"])
data class DreamLocationCrossRef(
    val dreamId: String,
    val locationId: Int
)
