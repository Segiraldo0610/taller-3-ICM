package com.example.taller3firebase.map

import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile

data class MapUserLayer(
    val profile: UserProfile,
    val isCurrentUser: Boolean,
    val route: List<RoutePoint>,
)

object MapRenderLogic {
    fun layers(users: List<UserProfile>, currentUserId: String): List<MapUserLayer> = users
        .filter(UserProfile::hasLocation)
        .sortedByDescending { it.uid == currentUserId }
        .map { profile ->
            MapUserLayer(
                profile = profile,
                isCurrentUser = profile.uid == currentUserId,
                route = profile.route,
            )
        }

    fun markerInitial(name: String): String = name.trim().firstOrNull()?.uppercase() ?: "?"
}

