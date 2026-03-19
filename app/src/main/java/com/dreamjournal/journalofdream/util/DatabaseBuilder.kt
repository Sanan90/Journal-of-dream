package com.dreamjournal.journalofdream.util

import android.content.Context
import com.dreamjournal.journalofdream.database.AppDatabase

/**
 * Legacy wrapper kept for backward compatibility.
 * Use AppDatabase.getInstance(context) directly in new code.
 */
object DatabaseBuilder {
    fun getInstance(context: Context): AppDatabase = AppDatabase.getInstance(context)
}
