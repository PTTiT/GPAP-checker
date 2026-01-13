package com.geocomply.test.gpapchecker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocomply.test.gpapchecker.data.AppInfo
import com.geocomply.test.gpapchecker.data.UiState
import com.geocomply.test.gpapchecker.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    
    private val _uiState = MutableLiveData<UiState<List<AppInfo>>>(UiState.Initial)
    val uiState: LiveData<UiState<List<AppInfo>>> = _uiState
    
    private val _canExport = MutableLiveData<Boolean>()
    val canExport: LiveData<Boolean> = _canExport
    
    init {
        _canExport.value = false
    }
    
    fun checkPackages() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val results = repository.checkPackages()
                _uiState.value = UiState.Success(results)
                _canExport.value = results.isNotEmpty()
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error checking packages: ${e.message}")
                _canExport.value = false
            }
        }
    }
    
    fun exportResults() {
        val currentState = _uiState.value
        if (currentState is UiState.Success && currentState.data.isNotEmpty()) {
            repository.exportToCsv(currentState.data)
        }
    }
    
    fun getPackageCount(): Int {
        return repository.getPackageNamesToCheck().size
    }
    
    fun getCurrentAppList(): List<AppInfo>? {
        return when (val state = _uiState.value) {
            is UiState.Success -> state.data
            else -> null
        }
    }
}
