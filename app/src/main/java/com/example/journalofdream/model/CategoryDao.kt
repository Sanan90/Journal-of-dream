package com.example.journalofdream.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    // Возвращает дефолтные категории + кастомные категории текущего пользователя
    @Query("SELECT * FROM categories WHERE ownerUid = 'default' OR ownerUid = :ownerUid ORDER BY isCustom ASC, name ASC")
    fun getCategoriesForUser(ownerUid: String): LiveData<List<Category>>

    // Одноразовый запрос для проверки существования дефолтных категорий
    @Query("SELECT * FROM categories WHERE ownerUid = 'default'")
    suspend fun getDefaultCategoriesOnce(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    // Удалить все кастомные категории пользователя (при выходе из аккаунта не нужно, но пригодится)
    @Query("DELETE FROM categories WHERE ownerUid = :ownerUid AND isCustom = 1")
    suspend fun deleteCustomCategoriesForUser(ownerUid: String)
}
