package com.example.journalofdream.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

/**
 * Кросс-ссылка многие-ко-многим между снами и локациями.
 * Связи:
 * dreamId -> Dream.localId
 * locationId -> Location.id
 */
@Entity(
    tableName = "dream_location_cross_ref",
    primaryKeys = ["dreamId", "locationId"],
    foreignKeys = [
        ForeignKey(
            entity = Dream::class,
            parentColumns = ["localId"],    // ИСПРАВЛЕНО: ссылаемся на поле localId у Dream (ранее могло быть "id")
            childColumns = ["dreamId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],         // ссылаемся на поле id у Location
            childColumns = ["locationId"],
            onDelete = CASCADE
        )
    ]
)
data class DreamLocationCrossRef(
    val dreamId: Int,
    val locationId: Int
)
