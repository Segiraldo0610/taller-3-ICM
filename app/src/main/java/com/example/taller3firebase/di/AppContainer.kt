package com.example.taller3firebase.di

import android.content.Context
import com.example.taller3firebase.data.FirebaseAuthRepository
import com.example.taller3firebase.data.FirebaseProfileRepository
import com.example.taller3firebase.data.FirebaseTrackingRepository
import com.example.taller3firebase.data.FusedLocationTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppContainer(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    val authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance())
    val profileRepository = FirebaseProfileRepository(
        firestore = firestore,
        storage = FirebaseStorage.getInstance(),
    )
    val trackingRepository = FirebaseTrackingRepository(firestore)
    val locationTracker = FusedLocationTracker(context.applicationContext)
}

