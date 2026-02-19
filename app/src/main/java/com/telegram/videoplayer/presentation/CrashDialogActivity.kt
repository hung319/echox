package com.telegram.videoplayer.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.telegram.videoplayer.CrashHandler
import com.telegram.videoplayer.R

class CrashDialogActivity : AppCompatActivity() {
    
    private lateinit var crashLog: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        setContentView(R.layout.activity_crash_dialog)
        
        // Get crash log from intent or saved prefs
        crashLog = intent.getStringExtra("crash_log") 
            ?: CrashHandler.getCrashLog(this) 
            ?: "No crash log available"
        
        setupViews()
    }
    
    private fun setupViews() {
        // Display crash log
        val logTextView = findViewById<android.widget.TextView>(R.id.crash_log_text)
        logTextView.text = crashLog
        
        // Copy button
        findViewById<MaterialButton>(R.id.btn_copy).setOnClickListener {
            copyToClipboard()
        }
        
        // Close button
        findViewById<MaterialButton>(R.id.btn_close).setOnClickListener {
            finishAffinity()
        }
        
        // Restart button
        findViewById<MaterialButton>(R.id.btn_restart).setOnClickListener {
            restartApp()
        }
    }
    
    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Crash Log", crashLog)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.crash_copied, Toast.LENGTH_SHORT).show()
    }
    
    private fun restartApp() {
        CrashHandler.clearCrash(this)
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back, must use buttons
    }
}
