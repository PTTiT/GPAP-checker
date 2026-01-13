package com.geocomply.test.gpapchecker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geocomply.test.gpapchecker.repository.AppRepository
import com.geocomply.test.gpapchecker.utils.AppChecker
import com.geocomply.test.gpapchecker.utils.CsvExporter

class MainViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val appChecker = AppChecker(context)
            val csvExporter = CsvExporter(context)
            val repository = AppRepository(appChecker, csvExporter)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException(context.getString(com.geocomply.test.gpapchecker.R.string.unknown_viewmodel_class))
    }
}
