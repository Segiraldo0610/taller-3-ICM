package com.example.taller3firebase.domain

data class RoutePoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
)

object RouteHistory {
    const val MAX_POINTS = 500

    fun limit(points: List<RoutePoint>, maximum: Int = MAX_POINTS): List<RoutePoint> =
        if (points.size <= maximum) points else points.takeLast(maximum)
}

