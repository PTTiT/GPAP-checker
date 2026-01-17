package com.geocomply.test.gpapchecker.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.data.ImportState
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportViewModel(
    private val repository: PackageListRepository,
    private val parser: PackageListParser,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _importState = MutableLiveData<ImportState>(ImportState.Initial)
    val importState: LiveData<ImportState> = _importState

    fun parseTextInput(input: String) {
        viewModelScope.launch {
            _importState.value = ImportState.ParsingInput
            val result = withContext(Dispatchers.Default) {
                parser.parse(input)
            }
            _importState.value = ImportState.ValidationComplete(result)
        }
    }

    fun parseFileContent(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.ReadingFile
            try {
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: throw IllegalStateException("Failed to read file")
                }

                _importState.value = ImportState.ParsingInput
                val result = withContext(Dispatchers.Default) {
                    parser.parse(content)
                }
                _importState.value = ImportState.ValidationComplete(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to read file: ${e.message}")
            }
        }
    }

    fun performImport(packages: List<String>, mode: ImportMode) {
        viewModelScope.launch {
            try {
                repository.savePackages(packages, mode)
                _importState.value = ImportState.ImportSuccess(
                    packageCount = packages.size,
                    mode = mode
                )
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Initial
    }
}
