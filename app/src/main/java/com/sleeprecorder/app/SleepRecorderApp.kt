package com.sleeprecorder.app

import android.app.Application
import com.sleeprecorder.app.data.AppDatabase

class SleepRecorderApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}