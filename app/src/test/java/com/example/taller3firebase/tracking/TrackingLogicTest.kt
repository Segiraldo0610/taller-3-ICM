package com.example.taller3firebase.tracking

import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingLogicTest {
    @Test
    fun `a location update connects the user and appends its route`() {
        val user = UserProfile(uid = "samuel", route = listOf(RoutePoint(4.6, -74.1, 1L)))
        val point = RoutePoint(4.61, -74.09, 2L)

        val updated = TrackingLogic.withLocation(user, point)

        assertTrue(updated.connected)
        assertEquals(4.61, updated.latitude ?: 0.0, 0.0)
        assertEquals(listOf(1L, 2L), updated.route.map { it.timestamp })
    }

    @Test
    fun `disconnecting clears the route`() {
        val user = UserProfile(uid = "samuel", connected = true, route = listOf(RoutePoint()))

        val updated = TrackingLogic.disconnected(user)

        assertFalse(updated.connected)
        assertTrue(updated.route.isEmpty())
    }

    @Test
    fun `visible users require location and are limited to one hundred`() {
        val users = (0..105).map { index ->
            UserProfile(uid = "$index", connected = true, latitude = 4.0, longitude = -74.0)
        } + UserProfile(uid = "missing", connected = true)

        val visible = TrackingLogic.visibleUsers(users)

        assertEquals(100, visible.size)
        assertTrue(visible.all { it.hasLocation })
    }
}

