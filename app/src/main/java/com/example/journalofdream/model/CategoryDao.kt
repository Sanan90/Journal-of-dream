package com.example.journalofdream.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)


    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<Category>
}
