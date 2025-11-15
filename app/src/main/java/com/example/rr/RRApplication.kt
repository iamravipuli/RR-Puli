package com.example.rr

import android.app.Application
import com.google.firebase.FirebaseApp

class RRApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
