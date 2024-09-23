package com.example.journalofdream.util

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.journalofdream.database.AppDatabase

object DatabaseBuilder {
    private var INSTANCE: AppDatabase? = null

    // Определение миграции с версии 1 на версию 2
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Ваш существующий код миграции
        }
    }

    // Определение миграции с версии 2 на версию 3
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Проверяем, существует ли столбец 'locationId' в таблице 'dreams'
            val cursor = database.query("PRAGMA table_info(dreams)")
            var columnExists = false
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (columnName == "locationId") {
                    columnExists = true
                    break
                }
            }
            cursor.close()

            // Если столбец не существует, добавляем его
            if (!columnExists) {
                database.execSQL("ALTER TABLE dreams ADD COLUMN locationId INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }

    // Определение миграции с версии 3 на версию 4
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Проверяем, существует ли столбец 'category' в таблице 'dreams'
            val cursor = database.query("PRAGMA table_info(dreams)")
            var columnExists = false
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (columnName == "category") {
                    columnExists = true
                    break
                }
            }
            cursor.close()

            // Если столбец не существует, добавляем его
            if (!columnExists) {
                database.execSQL("ALTER TABLE dreams ADD COLUMN category TEXT NOT NULL DEFAULT 'Без категории'")
            }
        }
    }

    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dreams.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Добавьте, если не хотите определять миграции
                    .build()
            }
        }
        return INSTANCE!!
    }
}
