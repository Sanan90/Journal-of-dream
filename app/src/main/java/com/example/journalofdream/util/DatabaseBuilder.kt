package com.example.journalofdream.util

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.journalofdream.model.DreamDatabase

object DatabaseBuilder {
    private var INSTANCE: DreamDatabase? = null

    // Определение миграции с версии 1 на версию 2
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `locations_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `description` TEXT NOT NULL
                )
            """.trimIndent())

            database.execSQL("""
                INSERT INTO `locations_new` (`id`, `name`, `description`)
                SELECT `id`, `name`, `description` FROM `locations`
            """.trimIndent())

            database.execSQL("DROP TABLE `locations`")
            database.execSQL("ALTER TABLE `locations_new` RENAME TO `locations`")
        }
    }

    // Определение миграции с версии 2 на версию 3
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Добавляем новый столбец locationId в таблицу dreams
            database.execSQL("ALTER TABLE dreams ADD COLUMN locationId INTEGER DEFAULT 0 NOT NULL")
        }
    }

    fun getInstance(context: Context): DreamDatabase {
        if (INSTANCE == null) {
            synchronized(DreamDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    DreamDatabase::class.java,
                    "dreams.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)  // Добавляем миграции сюда
                    .build()
            }
        }
        return INSTANCE!!
    }
}
