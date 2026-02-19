package com.telegram.videoplayer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import com.telegram.videoplayer.presentation.CrashDialogActivity
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    companion object {
        private const val PREFS_NAME = "crash_prefs"
        private const val KEY_CRASH_LOG = "crash_log"
        private const val KEY_HAS_CRASH = "has_crash"
        
        fun init(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext))
        }
        
        fun hasCrash(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HAS_CRASH, false)
        }
        
        fun getCrashLog(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_CRASH_LOG, null)
        }
        
        fun clearCrash(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                remove(KEY_CRASH_LOG)
                remove(KEY_HAS_CRASH)
                apply()
            }
        }
    }
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Build crash log
        val crashLog = buildCrashLog(throwable)
        
        // Save crash log to SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CRASH_LOG, crashLog)
            .putBoolean(KEY_HAS_CRASH, true)
            .apply()
        
        // Launch crash dialog activity
        val intent = Intent(context, CrashDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("crash_log", crashLog)
        }
        context.startActivity(intent)
        
        // Kill the process
        Process.killProcess(Process.myPid())
        System.exit(1)
        
        // Call default handler (may not reach here)
        defaultHandler?.uncaughtException(thread, throwable)
    }
    
    private fun buildCrashLog(throwable: Throwable): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("         ECHOX CRASH REPORT")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        sb.appendLine("📅 Time: ${dateFormat.format(Date())}")
        sb.appendLine()
        sb.appendLine("📱 Device Info:")
        sb.appendLine("   Brand: ${Build.BRAND}")
        sb.appendLine("   Device: ${Build.DEVICE}")
        sb.appendLine("   Model: ${Build.MODEL}")
        sb.appendLine("   Product: ${Build.PRODUCT}")
        sb.appendLine("   Manufacturer: ${Build.MANUFACTURER}")
        sb.appendLine()
        sb.appendLine("🤖 Android Info:")
        sb.appendLine("   SDK: ${Build.VERSION.SDK_INT}")
        sb.appendLine("   Release: ${Build.VERSION.RELEASE}")
        sb.appendLine("   Codename: ${Build.VERSION.CODENAME}")
        sb.appendLine()
        sb.appendLine("📦 App Info:")
        sb.appendLine("   Package: ${context.packageName}")
        sb.appendLine("   Version: ${getAppVersion()}")
        sb.appendLine()
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("           STACK TRACE")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        
        // Print full stack trace
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        sb.append(sw.toString())
        
        sb.appendLine()
        sb.appendLine("═══════════════════════════════════════")
        
        return sb.toString()
    }
    
    private fun getAppVersion(): String {
        return try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pi.versionName} (${pi.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
