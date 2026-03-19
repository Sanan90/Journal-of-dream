package com.dreamjournal.journalofdream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.Location
import com.dreamjournal.journalofdream.model.DreamCharacter
import com.dreamjournal.journalofdream.model.DreamLocationCrossRef
import com.dreamjournal.journalofdream.model.DreamCharacterCrossRef
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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("PRAGMA foreign_keys=OFF")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `dreams_new` (
                `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ownerUid` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `time` TEXT NOT NULL,
                `category` TEXT NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT INTO `dreams_new` (`localId`, `ownerUid`, `title`, `content`, `date`, `time`, `category`)
            SELECT `localId`, `ownerUid`, `title`, `content`, `date`, `time`, `category`
            FROM `dreams`
            """.trimIndent()
        )

        database.execSQL("DROP TABLE `dreams`")
        database.execSQL("ALTER TABLE `dreams_new` RENAME TO `dreams`")
        database.execSQL("PRAGMA foreign_keys=ON")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `characters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerUid` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL)"
        )
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `dream_character_cross_ref` (`dreamId` INTEGER NOT NULL, `characterId` INTEGER NOT NULL, PRIMARY KEY(`dreamId`, `characterId`), FOREIGN KEY(`dreamId`) REFERENCES `dreams`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
    }
}

@Database(
    entities = [
        Dream::class,
        Location::class,
        DreamCharacter::class,
        DreamLocationCrossRef::class,
        DreamCharacterCrossRef::class,
        Category::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dreamDao(): DreamDao
    abstract fun locationDao(): LocationDao
    abstract fun characterDao(): CharacterDao
    abstract fun categoryDao(): CategoryDao

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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
