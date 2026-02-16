package com.telegram.videoplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TelegramVideoPlayerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize TDLib native library
        System.loadLibrary("tdjni")
    }
}
