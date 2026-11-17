package com.example.taller3firebase.data

import android.net.Uri
import com.example.taller3firebase.domain.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

interface ProfileRepository {
    suspend fun createProfile(profile: UserProfile, photoUri: Uri)
}

class FirebaseProfileRepository(
    private val firestore: FirebaseFirestore,
    private val photoUploader: PhotoUploader,
) : ProfileRepository {
    override suspend fun createProfile(profile: UserProfile, photoUri: Uri) {
        val photoUrl = photoUploader.upload(profile.uid, photoUri)
        val document = profile.toFirestoreDocument(photoUrl)
        firestore.collection(USERS_COLLECTION).document(profile.uid).set(document).awaitResult()
    }

    companion object {
        const val USERS_COLLECTION = "users"
    }
}

internal fun UserProfile.toFirestoreDocument(photoUrl: String): Map<String, Any?> = mapOf(
    "uid" to uid,
    "name" to name,
    "identification" to identification,
    "email" to email,
    "phone" to phone,
    "photoUrl" to photoUrl,
    "latitude" to null,
    "longitude" to null,
    "connected" to false,
    "route" to emptyList<Map<String, Any>>(),
)

