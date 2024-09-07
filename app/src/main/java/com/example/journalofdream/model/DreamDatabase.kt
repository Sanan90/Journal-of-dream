package com.example.journalofdream.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Dream::class, Location::class], version = 3)
abstract class DreamDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun locationDao(): LocationDao
}
