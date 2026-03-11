package com.dreamjournal.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.Category
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao = AppDatabase.getInstance(application).categoryDao()
    private val auth = FirebaseAuth.getInstance()

    // Текущий uid — пользователь или гость
    private val _ownerUid = MutableLiveData<String>(auth.currentUser?.uid ?: "guest")

    // Показываем дефолтные категории + кастомные текущего пользователя
    val allCategories: LiveData<List<Category>> = _ownerUid.switchMap { uid ->
        categoryDao.getCategoriesForUser(uid)
    }

    init {
        // При первом запуске вставляем дефолтные категории если их ещё нет
        viewModelScope.launch {
            val existing = categoryDao.getDefaultCategoriesOnce()
            if (existing.isEmpty()) {
                val defaultCategories = listOf(
                    Category(name = "Кошмары",          isCustom = false, ownerUid = "default"),
                    Category(name = "Осознанные сны",   isCustom = false, ownerUid = "default"),
                    Category(name = "Сюжетные сны",     isCustom = false, ownerUid = "default"),
                    Category(name = "Личные сны",       isCustom = false, ownerUid = "default")
                )
                defaultCategories.forEach { categoryDao.insertCategory(it) }
            }
        }
    }

    // Вызывается при смене пользователя (логин/логаут)
    fun setOwner(uid: String) {
        _ownerUid.value = uid
    }

    // Добавить кастомную категорию для текущего пользователя
    fun addCategory(category: Category) {
        val uid = _ownerUid.value ?: "guest"
        viewModelScope.launch {
            categoryDao.insertCategory(category.copy(ownerUid = uid, isCustom = true))
        }
    }

    // Удалить категорию
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }
}
