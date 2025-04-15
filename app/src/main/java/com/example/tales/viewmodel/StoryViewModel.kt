package com.example.tales.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tales.model.Story
import com.example.tales.model.StoryPersonalization
import com.example.tales.model.StoryRequest
import com.example.tales.repository.StoryRepository
import com.example.tales.service.OpenAIService
import kotlinx.coroutines.launch

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val openAIService: OpenAIService
) : ViewModel() {
    
    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories
    
    private val _currentStory = MutableLiveData<Story?>()
    val currentStory: LiveData<Story?> = _currentStory
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadStories()
    }
    
    fun loadStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val storyList = storyRepository.getAllStories()
                _stories.value = storyList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des histoires: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getStoryById(storyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val story = storyRepository.getStoryById(storyId)
                _currentStory.value = story
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement de l'histoire: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createStory(personalization: StoryPersonalization, storyType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val storyRequest = StoryRequest(personalization, storyType)
                val response = openAIService.generateStory(storyRequest)
                
                if (response.success && response.data is Story) {
                    val story = response.data
                    val saved = storyRepository.saveStory(story)
                    
                    if (saved) {
                        _currentStory.value = story
                        loadStories() // Recharger la liste des histoires
                        _error.value = null
                    } else {
                        _error.value = "Erreur lors de l'enregistrement de l'histoire"
                    }
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la création de l'histoire: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
