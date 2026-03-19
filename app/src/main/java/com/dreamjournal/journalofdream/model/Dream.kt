package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Модель сна. Первичный ключ – localId.
 * ownerUid указывает, какому пользователю (или "guest") принадлежит сон.
 * locationIds и characterIds игнорируются Room, но используются для хранения связей в Firestore.
 * time — время записи сна в формате "HH:mm" (например "07:30").
 * Для старых записей без времени — пустая строка.
 */
@Entity(tableName = "dreams")
data class Dream(
    @PrimaryKey(autoGenerate = true)
    var localId: Int = 0,
    var ownerUid: String = "guest",
    var title: String = "",
    var content: String = "",
    var date: String = "",
    var time: String = "",   // "HH:mm" — время записи сна
    var category: String = "",
    @Ignore
    var locationIds: List<Int> = emptyList(),
    @Ignore
    var characterIds: List<Int> = emptyList()
)
