package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Category
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao = AppDatabase.getInstance(application).categoryDao()

    // Это список категорий из базы (как LiveData).
    // Используем, например, в UI (через observeAsState).
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    init {
        // При первом создании ViewModel проверяем, есть ли категории.
        viewModelScope.launch {
            // Метод, который возвращает список категорий без LiveData:
            // Нужно объявить его в CategoryDao (см. пример ниже).
            val existingCount = categoryDao.getAllCategoriesOnce().size

            // Если таблица пустая - добавим стандартные.
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

    // Метод для добавления новой категории (пользовательской, например).
    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.insertCategory(category)
        }
    }

    // Метод для удаления категории
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }
}