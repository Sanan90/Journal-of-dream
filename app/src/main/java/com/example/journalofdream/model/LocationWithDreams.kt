package com.example.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * "Локация со снами" (противоположная сторона связи).
 *
 * parentColumn = "id" (у Location)
 * entityColumn = "localId" (у Dream)
 * junction: locationId -> id, dreamId -> localId
 */
data class LocationWithDreams(
    @Embedded val location: Location,
    @Relation(
        parentColumn = "id",         // PK у Location
        entityColumn = "localId",    // PK у Dream
        associateBy = Junction(
            DreamLocationCrossRef::class,
            parentColumn = "locationId", // поле в CrossRef, указывающее на Location
            entityColumn = "dreamId"     // поле в CrossRef, указывающее на Dream
        )
    )
    val dreams: List<Dream>
)
