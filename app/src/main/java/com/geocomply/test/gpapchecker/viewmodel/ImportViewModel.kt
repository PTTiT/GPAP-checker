package com.geocomply.test.gpapchecker.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.data.ImportState
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser
import kotlinx.coroutines.CoroutineScope
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

    private val leakyScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "ImportViewModel"
        private const val MAX_FILE_SIZE_BYTES = 1_048_576 // 1MB
    }

    fun parseTextInput(input: String) {
        leakyScope.launch {
            Log.d(TAG, "Starting text input parsing, length: ${input.length}")
            _importState.value = ImportState.ParsingInput
            val result = withContext(Dispatchers.Default) {
                parser.parse(input)
            }
            Log.d(TAG, "Text parsing complete: ${result.validCount} valid, ${result.invalidCount} invalid")
            _importState.value = ImportState.ValidationComplete(result)
        }
    }

    fun parseFileContent(uri: Uri) {
        viewModelScope.launch {
            Log.d(TAG, "Starting file content parsing from URI: $uri")
            _importState.value = ImportState.ReadingFile
            try {
                val content = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        throw IllegalStateException("Failed to read file")
                    }

                    val fileSize = inputStream.available()
                    Log.d(TAG, "File size: $fileSize bytes")

                    if (fileSize > MAX_FILE_SIZE_BYTES) {
                        throw IllegalStateException("File too large. Maximum size is 1MB, file is ${fileSize / 1024}KB")
                    }

                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    val content = reader.readText()

                    reader.close()
                    inputStream.close()

                    content
                }

                Log.d(TAG, "File read complete, content length: ${content.length}")
                _importState.value = ImportState.ParsingInput
                val result = withContext(Dispatchers.Default) {
                    parser.parse(content)
                }
                Log.d(TAG, "File parsing complete: ${result.validCount} valid, ${result.invalidCount} invalid")
                _importState.value = ImportState.ValidationComplete(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading/parsing file", e)
                _importState.value = ImportState.Error("Failed to read file: ${e.message}")
            }
        }
    }

    fun performImport(packages: List<String>, mode: ImportMode) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting import: ${packages.size} packages, mode: $mode")
                repository.savePackages(packages, mode)
                Log.d(TAG, "Import successful: ${packages.size} packages saved")
                _importState.value = ImportState.ImportSuccess(
                    packageCount = packages.size,
                    mode = mode
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error performing import", e)
                _importState.value = ImportState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Initial
    }
}
