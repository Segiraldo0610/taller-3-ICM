package com.example.taller3firebase

import android.app.Application
import com.example.taller3firebase.di.AppContainer
import com.google.firebase.FirebaseApp

class Taller3Application : Application() {
    var firebaseReady: Boolean = false
        private set

    var container: AppContainer? = null
        private set

    override fun onCreate() {
        super.onCreate()
        firebaseReady = FirebaseApp.initializeApp(this) != null
        if (firebaseReady) container = AppContainer(this)
    }
}

