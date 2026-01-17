package com.geocomply.test.gpapchecker.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser

class ImportViewModelFactory(
    private val repository: PackageListRepository,
    private val parser: PackageListParser,
    private val contentResolver: ContentResolver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(repository, parser, contentResolver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
