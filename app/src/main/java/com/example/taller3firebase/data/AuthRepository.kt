package com.example.taller3firebase.data

import com.google.firebase.auth.FirebaseAuth

interface AuthRepository {
    val currentUserId: String?
    fun observeSession(listener: (String?) -> Unit): AutoCloseable
    suspend fun signIn(email: String, password: String)
    suspend fun register(email: String, password: String): String
    suspend fun deleteCurrentUser()
    fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
) : AuthRepository {
    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun observeSession(listener: (String?) -> Unit): AutoCloseable {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            listener(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(authListener)
        return AutoCloseable { auth.removeAuthStateListener(authListener) }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).awaitResult()
    }

    override suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitResult()
        return requireNotNull(result.user?.uid)
    }

    override suspend fun deleteCurrentUser() {
        auth.currentUser?.delete()?.awaitResult()
    }

    override fun signOut() = auth.signOut()
}

