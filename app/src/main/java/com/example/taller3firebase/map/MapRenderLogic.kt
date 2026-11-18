package com.example.taller3firebase.map

import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile

data class MapUserLayer(
    val profile: UserProfile,
    val isCurrentUser: Boolean,
    val route: List<RoutePoint>,
)

data class MapCoordinate(
    val latitude: Double,
    val longitude: Double,
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

    fun currentUserCenter(layers: List<MapUserLayer>): MapCoordinate? = layers
        .firstOrNull(MapUserLayer::isCurrentUser)
        ?.profile
        ?.let { profile ->
            val latitude = profile.latitude ?: return@let null
            val longitude = profile.longitude ?: return@let null
            MapCoordinate(latitude = latitude, longitude = longitude)
        }
}

