//package com.example.journalofdream.model
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.sqlite.db.SupportSQLiteDatabase
//import androidx.room.migration.Migration
//import com.example.journalofdream.database.DreamDao
//import com.example.journalofdream.database.LocationDao
//
//@Database(
//    entities = [Dream::class, Location::class, DreamLocationCrossRef::class],
//    version = 2,
//    exportSchema = false
//)
//abstract class DreamDatabase : RoomDatabase() {
//    abstract fun dreamDao(): DreamDao
//    abstract fun locationDao(): LocationDao
//    abstract fun categoryDao(): CategoryDao
//}
