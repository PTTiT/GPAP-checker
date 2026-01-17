package com.geocomply.test.gpapchecker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geocomply.test.gpapchecker.adapter.AppInfoAdapter
import com.geocomply.test.gpapchecker.data.UiState
import com.geocomply.test.gpapchecker.viewmodel.MainViewModel
import com.geocomply.test.gpapchecker.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppInfoAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        val factory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupToolbar()
        setupViews()
        setupObservers()
        setupButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        super.onResume()
        // Refresh the app list when returning from ImportActivity
        // This ensures the UI reflects any changes made to the package list
        val currentState = viewModel.uiState.value
        if (currentState is UiState.Success && currentState.data.isNotEmpty()) {
            // Re-check packages if we previously had results
            viewModel.checkPackages()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, ImportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.rv_app_list)
        progressBar = findViewById(R.id.progress_bar)
        errorTextView = findViewById(R.id.tv_error)
        
        adapter = AppInfoAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupObservers() {
        // Observe UI state changes
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Initial -> {
                    // Initial state - do nothing
                }
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    errorTextView.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    errorTextView.visibility = View.GONE
                    adapter.updateData(state.data)
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = state.message
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Observe export button state
        viewModel.canExport.observe(this) { canExport ->
            findViewById<Button>(R.id.btn_export_csv).isEnabled = canExport
        }
    }
    
    private fun setupButtons() {
        val checkButton = findViewById<Button>(R.id.btn_check_packages)
        val exportButton = findViewById<Button>(R.id.btn_export_csv)
        
        checkButton.setOnClickListener {
            viewModel.checkPackages()
        }
        
        exportButton.setOnClickListener {
            viewModel.exportResults()
        }
        
        // Initially disable export button
        exportButton.isEnabled = false
    }
}