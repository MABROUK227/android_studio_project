package com.example.tales.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tales.firebase.ComptineRepository
import com.example.tales.models.Comptine
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    
    private val repository = ComptineRepository()
    
    private val _comptines = MutableLiveData<List<Comptine>>()
    val comptines: LiveData<List<Comptine>> = _comptines
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadComptines() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = ""
                
                val comptinesList = repository.getAllComptines()
                _comptines.value = comptinesList
                
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des comptines: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
