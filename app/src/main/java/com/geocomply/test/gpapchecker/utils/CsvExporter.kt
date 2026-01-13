package com.geocomply.test.gpapchecker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.geocomply.test.gpapchecker.data.AppInfo
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExporter(private val context: Context) {
    
    fun exportAndEmail(appInfoList: List<AppInfo>) {
        val csvFile = createCsvFile(appInfoList)
        if (csvFile != null) {
            sendEmail(csvFile)
        }
    }
    
    private fun createCsvFile(appInfoList: List<AppInfo>): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "package_check_report_$timestamp.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("App Name,Package Name,License Activity Status,Installation Status\n")
                
                // Write data rows
                for (appInfo in appInfoList) {
                    val licenseStatus = when {
                        !appInfo.isInstalled -> "N/A"
                        appInfo.hasLicenseActivity -> "Has License Activity"
                        else -> "No License Activity"
                    }
                    
                    val installStatus = if (appInfo.isInstalled) "Installed" else "Not Found"
                    
                    writer.append("\"${appInfo.appName}\",")
                    writer.append("\"${appInfo.packageName}\",")
                    writer.append("\"$licenseStatus\",")
                    writer.append("\"$installStatus\"\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun sendEmail(csvFile: File) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
                putExtra(Intent.EXTRA_SUBJECT, "Package License Check Report")
                putExtra(Intent.EXTRA_TEXT, "Please find attached the package license check report.")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(emailIntent, "Send report via email"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}