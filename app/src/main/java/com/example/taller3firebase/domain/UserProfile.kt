package com.example.taller3firebase.domain

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val identification: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val connected: Boolean = false,
    val route: List<RoutePoint> = emptyList(),
) {
    val hasLocation: Boolean
        get() = latitude != null && longitude != null
}

