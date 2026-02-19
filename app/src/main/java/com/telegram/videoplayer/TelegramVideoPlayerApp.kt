package com.telegram.videoplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TelegramVideoPlayerApp : Application() {
    
    override fun onCreate() {
        // Initialize crash handler FIRST before anything else
        CrashHandler.init(this)
        
        super.onCreate()
        
        // Note: Removed System.loadLibrary("tdjni") - tdlight-stubs is Java-only
        // Native library is not needed for the stub implementation
    }
}
