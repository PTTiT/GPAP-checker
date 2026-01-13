package com.geocomply.test.gpapchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        setupButtons()
    }
    
    private fun setupButtons() {
        val packageCheckerButton = findViewById<Button>(R.id.btn_package_checker)
        val appsButton = findViewById<Button>(R.id.btn_apps)
        
        packageCheckerButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        
        appsButton.setOnClickListener {
            val intent = Intent(this, AppsActivity::class.java)
            startActivity(intent)
        }
    }
}