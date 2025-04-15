package com.example.tales.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tales.firebase.ComptineRepository
import com.example.tales.models.Comptine
import kotlinx.coroutines.launch

class ComptineDetailViewModel : ViewModel() {
    
    private val repository = ComptineRepository()
    
    private val _currentComptine = MutableLiveData<Comptine>()
    val currentComptine: LiveData<Comptine> = _currentComptine
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadComptine(comptineId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = ""
                
                val comptine = repository.getComptineById(comptineId)
                if (comptine != null) {
                    _currentComptine.value = comptine
                } else {
                    _error.value = "Comptine non trouvée"
                }
                
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement de la comptine: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
