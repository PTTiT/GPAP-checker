package com.geocomply.test.gpapchecker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.geocomply.test.gpapchecker.data.ImportMode
import com.geocomply.test.gpapchecker.data.ImportState
import com.geocomply.test.gpapchecker.repository.PackageListRepository
import com.geocomply.test.gpapchecker.utils.PackageListParser
import com.geocomply.test.gpapchecker.utils.PackageListStorage
import com.geocomply.test.gpapchecker.viewmodel.ImportViewModel
import com.geocomply.test.gpapchecker.viewmodel.ImportViewModelFactory

class ImportActivity : AppCompatActivity() {

    private val viewModel: ImportViewModel by viewModels {
        val storage = PackageListStorage(applicationContext)
        val repository = PackageListRepository(storage)
        val parser = PackageListParser()
        ImportViewModelFactory(repository, parser, contentResolver)
    }

    private lateinit var textInputField: EditText
    private lateinit var importFileButton: Button
    private lateinit var importModeGroup: RadioGroup
    private lateinit var validationSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var importButton: Button

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.parseFileContent(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        bindViews()
        setupListeners()
        observeViewModel()
    }

    private fun bindViews() {
        textInputField = findViewById(R.id.textInputField)
        importFileButton = findViewById(R.id.importFileButton)
        importModeGroup = findViewById(R.id.importModeGroup)
        validationSummary = findViewById(R.id.validationSummary)
        progressBar = findViewById(R.id.progressBar)
        errorMessage = findViewById(R.id.errorMessage)
        importButton = findViewById(R.id.importButton)
    }

    private fun setupListeners() {
        textInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.parseTextInput(s.toString())
            }
        })

        importFileButton.setOnClickListener {
            filePickerLauncher.launch(arrayOf("text/plain"))
        }

        importButton.setOnClickListener {
            val state = viewModel.importState.value
            if (state is ImportState.ValidationComplete) {
                val mode = if (findViewById<RadioButton>(R.id.radioReplace).isChecked) {
                    ImportMode.REPLACE
                } else {
                    ImportMode.APPEND
                }
                viewModel.performImport(state.result.validPackages, mode)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.importState.observe(this) { state ->
            when (state) {
                is ImportState.Initial -> showInitialState()
                is ImportState.ParsingInput -> showParsingState()
                is ImportState.ReadingFile -> showReadingFileState()
                is ImportState.ValidationComplete -> showValidationResults(state)
                is ImportState.ImportSuccess -> showImportSuccess(state)
                is ImportState.Error -> showError(state.message)
            }
        }
    }

    private fun showInitialState() {
        progressBar.visibility = View.GONE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
        importButton.isEnabled = false
    }

    private fun showParsingState() {
        progressBar.visibility = View.VISIBLE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showReadingFileState() {
        progressBar.visibility = View.VISIBLE
        validationSummary.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showValidationResults(state: ImportState.ValidationComplete) {
        progressBar.visibility = View.GONE
        errorMessage.visibility = View.GONE

        val result = state.result
        validationSummary.text = getString(
            R.string.validation_summary,
            result.validCount,
            result.invalidCount
        )
        validationSummary.visibility = View.VISIBLE

        importButton.isEnabled = result.hasValidPackages
    }

    private fun showImportSuccess(state: ImportState.ImportSuccess) {
        Toast.makeText(
            this,
            "Imported ${state.packageCount} packages",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate back to MainActivity (GPAP Checker screen)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
        importButton.isEnabled = false
    }
}
