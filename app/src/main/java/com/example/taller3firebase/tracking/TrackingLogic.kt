package com.example.taller3firebase.tracking

import com.example.taller3firebase.domain.RouteHistory
import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile

object TrackingLogic {
    fun withLocation(user: UserProfile, point: RoutePoint): UserProfile = user.copy(
        latitude = point.latitude,
        longitude = point.longitude,
        connected = true,
        route = RouteHistory.limit(user.route + point),
    )

    fun disconnected(user: UserProfile): UserProfile = user.copy(
        connected = false,
        route = emptyList(),
    )

    fun visibleUsers(users: List<UserProfile>): List<UserProfile> = users
        .asSequence()
        .filter { it.connected && it.hasLocation }
        .take(MAX_VISIBLE_USERS)
        .toList()

    const val MAX_VISIBLE_USERS = 100
}

