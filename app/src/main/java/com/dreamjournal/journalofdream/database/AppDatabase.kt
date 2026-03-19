package com.dreamjournal.journalofdream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.Location
import com.dreamjournal.journalofdream.model.DreamLocationCrossRef
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.model.CategoryDao

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // no-op: reserved for legacy versions
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val cursor = database.query("PRAGMA table_info(dreams)")
        var columnExists = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "locationId") {
                columnExists = true
                break
            }
        }
        cursor.close()
        if (!columnExists) {
            database.execSQL("ALTER TABLE dreams ADD COLUMN locationId INTEGER DEFAULT 0 NOT NULL")
        }
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val cursor = database.query("PRAGMA table_info(dreams)")
        var columnExists = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "category") {
                columnExists = true
                break
            }
        }
        cursor.close()
        if (!columnExists) {
            database.execSQL("ALTER TABLE dreams ADD COLUMN category TEXT NOT NULL DEFAULT 'Без категории'")
        }
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val dreamsCursor = database.query("PRAGMA table_info(dreams)")
        var hasTime = false
        while (dreamsCursor.moveToNext()) {
            if (dreamsCursor.getString(dreamsCursor.getColumnIndexOrThrow("name")) == "time") {
                hasTime = true
                break
            }
        }
        dreamsCursor.close()
        if (!hasTime) {
            database.execSQL("ALTER TABLE dreams ADD COLUMN time TEXT NOT NULL DEFAULT ''")
        }

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isCustom` INTEGER NOT NULL, `ownerUid` TEXT NOT NULL, `color` TEXT NOT NULL)"
        )
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
