package com.example.journalofdream.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Модель сна. Первичный ключ – localId.
 * ownerUid указывает, какому пользователю (или "guest") принадлежит сон.
 * locationIds игнорируется Room, но используется для хранения связей в Firestore.
 */
@Entity(tableName = "dreams")
data class Dream(
    @PrimaryKey(autoGenerate = true)
    var localId: Int = 0,      // PK (автоинкремент) для Dream в локальной БД
    var ownerUid: String = "guest",
    var title: String = "",
    var content: String = "",
    var date: String = "",
    var category: String = "",
    @Ignore
    var locationIds: List<Int> = emptyList()  // ИСПРАВЛЕНО: список ID локаций (игнорируется Room, используется для Firestore)
)
