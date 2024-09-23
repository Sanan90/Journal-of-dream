package com.example.journalofdream.model

import androidx.room.Entity

@Entity(primaryKeys = ["dreamId", "locationId"])
data class DreamLocationCrossRef(
    val dreamId: Int,
    val locationId: Int
)