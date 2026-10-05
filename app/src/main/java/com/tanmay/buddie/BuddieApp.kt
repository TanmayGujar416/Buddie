package com.tanmay.buddie

import android.app.Application
import com.tanmay.buddie.domain.sync.MoodleSyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BuddieApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MoodleSyncWorker.schedule(this)
    }
}
