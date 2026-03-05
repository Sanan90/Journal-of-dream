package com.example.journalofdream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.model.DreamLocationCrossRef
import com.example.journalofdream.model.Category
import com.example.journalofdream.model.CategoryDao

// Миграция 4 → 5: добавляем колонку time в таблицу dreams
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE dreams ADD COLUMN time TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [
        Dream::class,
        Location::class,
        DreamLocationCrossRef::class,
        Category::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dreamDao(): DreamDao
    abstract fun locationDao(): LocationDao
    abstract fun categoryDao(): CategoryDao  // <- Вот метод для CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dream_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
