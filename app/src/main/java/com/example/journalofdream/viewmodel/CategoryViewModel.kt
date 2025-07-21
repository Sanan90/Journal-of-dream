package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Category
import kotlinx.coroutines.launch

/**
 * ViewModel для работы со списком категорий (Category).
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    // Берём DAO из AppDatabase
    private val categoryDao = AppDatabase.getInstance(application).categoryDao()

    // LiveData со всеми категориями (через DAO)
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    init {
        // При первом создании добавляем дефолтные категории, если база пустая
        viewModelScope.launch {
            val existingCount = categoryDao.getAllCategoriesOnce().size
            if (existingCount == 0) {
                val defaultCategories = listOf(
                    Category(name = "Без категории", isCustom = false),
                    Category(name = "Кошмары", isCustom = false),
                    Category(name = "Осознанные сны", isCustom = false),
                    Category(name = "Сюжетные сны", isCustom = false),
                    Category(name = "Личные сны", isCustom = false)
                )
                defaultCategories.forEach { cat ->
                    categoryDao.insertCategory(cat)
                }
            }
        }
    }

    // Добавить категорию (пользовательскую)
    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insertCategory(category)
        }
    }

    // Удалить категорию
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }
}
