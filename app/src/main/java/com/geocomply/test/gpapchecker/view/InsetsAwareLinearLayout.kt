package com.geocomply.test.gpapchecker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InsetsAwareLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        // Enable system insets handling
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top inset to prevent status bar overlap
            setPadding(
                paddingLeft,
                systemBars.top + paddingTop,
                paddingRight,
                paddingBottom
            )
            
            // Return the insets to continue the chain
            insets
        }
    }
}
