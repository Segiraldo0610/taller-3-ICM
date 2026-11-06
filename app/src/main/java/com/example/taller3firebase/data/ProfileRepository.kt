package com.example.taller3firebase.data

import android.net.Uri
import com.example.taller3firebase.domain.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

interface ProfileRepository {
    suspend fun createProfile(profile: UserProfile, photoUri: Uri)
}

class FirebaseProfileRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : ProfileRepository {
    override suspend fun createProfile(profile: UserProfile, photoUri: Uri) {
        val photoReference = storage.reference.child("profile_photos/${profile.uid}.jpg")
        photoReference.putFile(photoUri).awaitResult()
        val photoUrl = photoReference.downloadUrl.awaitResult().toString()

        val document = mapOf(
            "uid" to profile.uid,
            "name" to profile.name,
            "identification" to profile.identification,
            "email" to profile.email,
            "phone" to profile.phone,
            "photoUrl" to photoUrl,
            "latitude" to null,
            "longitude" to null,
            "connected" to false,
            "route" to emptyList<Map<String, Any>>(),
        )
        firestore.collection(USERS_COLLECTION).document(profile.uid).set(document).awaitResult()
    }

    companion object {
        const val USERS_COLLECTION = "users"
    }
}

