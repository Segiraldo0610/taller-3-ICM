package com.example.taller3firebase.di

import android.content.Context
import com.example.taller3firebase.data.FirebaseAuthRepository
import com.example.taller3firebase.data.FirebaseProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppContainer(context: Context) {
    val authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance())
    val profileRepository = FirebaseProfileRepository(
        firestore = FirebaseFirestore.getInstance(),
        storage = FirebaseStorage.getInstance(),
    )
}

