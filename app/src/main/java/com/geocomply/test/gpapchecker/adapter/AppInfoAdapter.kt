package com.geocomply.test.gpapchecker.adapter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.geocomply.test.gpapchecker.R
import com.geocomply.test.gpapchecker.data.AppInfo

class AppInfoAdapter(private var appList: List<AppInfo>) : 
    RecyclerView.Adapter<AppInfoAdapter.AppInfoViewHolder>() {
    
    class AppInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val index: TextView = itemView.findViewById(R.id.tv_index)
        val appName: TextView = itemView.findViewById(R.id.tv_app_name)
        val packageName: TextView = itemView.findViewById(R.id.tv_package_name)
        val licenseStatus: TextView = itemView.findViewById(R.id.tv_license_status)
        val installButton: Button = itemView.findViewById(R.id.btn_install)
        val cardView: CardView = itemView as CardView
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_info, parent, false)
        return AppInfoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AppInfoViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.index.text = "${position + 1}."
        holder.appName.text = appInfo.appName
        holder.packageName.text = appInfo.packageName
        
        when {
            !appInfo.isInstalled -> {
                holder.licenseStatus.text = "Not Found"
                holder.licenseStatus.setTextColor(Color.GRAY)
                holder.cardView.setCardBackgroundColor(Color.WHITE)
                holder.licenseStatus.setBackgroundColor(Color.TRANSPARENT)
                
                // Show install button and set click listener
                holder.installButton.visibility = View.VISIBLE
                holder.installButton.setOnClickListener {
                    openPlayStore(it, appInfo.packageName)
                }
            }
            appInfo.hasLicenseActivity -> {
                holder.licenseStatus.text = "⚠️ HAS LICENSE ACTIVITY"
                holder.licenseStatus.setTextColor(Color.WHITE)
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFD32F2F")) // Red background
                holder.licenseStatus.setBackgroundColor(Color.parseColor("#B71C1C")) // Darker red for text background
                
                // Hide install button for installed apps
                holder.installButton.visibility = View.GONE
            }
            else -> {
                holder.licenseStatus.text = "No License Activity"
                holder.licenseStatus.setTextColor(Color.parseColor("#4CAF50")) // Green text
                holder.cardView.setCardBackgroundColor(Color.WHITE)
                holder.licenseStatus.setBackgroundColor(Color.TRANSPARENT)
                
                // Hide install button for installed apps
                holder.installButton.visibility = View.GONE
            }
        }
    }
    
    override fun getItemCount(): Int = appList.size
    
    fun updateData(newAppList: List<AppInfo>) {
        appList = newAppList
        notifyDataSetChanged()
    }
    
    private fun openPlayStore(view: View, packageName: String) {
        try {
            // Try to open the Play Store app directly
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            view.context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            // If Play Store app is not available, open in browser
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                view.context.startActivity(browserIntent)
            } catch (e: Exception) {
                // Handle error silently - maybe show a toast in production
            }
        }
    }
}