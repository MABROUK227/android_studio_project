package com.example.tales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tales.repository.StoryRepository
import com.example.tales.service.OpenAIService

class ViewModelFactory(
    private val storyRepository: StoryRepository,
    private val openAIService: OpenAIService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(storyRepository, openAIService) as T
            }
            else -> throw IllegalArgumentException("ViewModel inconnu: ${modelClass.name}")
        }
    }
}
