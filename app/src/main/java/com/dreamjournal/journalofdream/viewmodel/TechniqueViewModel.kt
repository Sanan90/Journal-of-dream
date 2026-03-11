package com.dreamjournal.journalofdream.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamjournal.journalofdream.model.Technique
import com.dreamjournal.journalofdream.sync.TechniqueRepository
import kotlinx.coroutines.launch

class TechniqueViewModel : ViewModel() {
    private val repository = TechniqueRepository()

    private val _techniques = MutableLiveData<List<Technique>>(emptyList())
    val techniques: LiveData<List<Technique>> get() = _techniques

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Голоса текущего пользователя: techniqueId -> "like"/"dislike"
    private val _userVotes = MutableLiveData<Map<String, String>>(emptyMap())
    val userVotes: LiveData<Map<String, String>> get() = _userVotes

    init {
        repository.startListening { list ->
            _techniques.postValue(list)
            _isLoading.postValue(false)
        }
        loadUserVotes()
    }

    // Загружаем голоса при старте и при смене аккаунта
    fun loadUserVotes() {
        viewModelScope.launch {
            val votes = repository.getAllUserVotes()
            _userVotes.postValue(votes)
        }
    }

    fun addTechnique(name: String, description: String, source: String) {
        viewModelScope.launch {
            repository.addTechnique(Technique(
                name = name.trim(),
                description = description.trim(),
                source = source.trim()
            ))
        }
    }

    fun updateTechnique(technique: Technique, name: String, description: String, source: String) {
        viewModelScope.launch {
            repository.updateTechnique(technique.copy(
                name = name.trim(),
                description = description.trim(),
                source = source.trim()
            ))
        }
    }

    fun deleteTechnique(id: String) {
        viewModelScope.launch {
            repository.deleteTechnique(id)
        }
    }

    fun vote(techniqueId: String, isLike: Boolean) {
        val previousVote = _userVotes.value?.get(techniqueId)
        val newVote = if (isLike) "like" else "dislike"

        // Обновляем локально сразу для быстрого отклика UI
        val updatedVotes = _userVotes.value?.toMutableMap() ?: mutableMapOf()
        if (previousVote == newVote) {
            updatedVotes.remove(techniqueId) // снимаем голос
        } else {
            updatedVotes[techniqueId] = newVote
        }
        _userVotes.postValue(updatedVotes)

        viewModelScope.launch {
            repository.vote(techniqueId, isLike, previousVote)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
    }
}
