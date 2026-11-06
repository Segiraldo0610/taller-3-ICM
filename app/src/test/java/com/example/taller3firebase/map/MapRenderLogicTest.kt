package com.example.taller3firebase.map

import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRenderLogicTest {
    @Test
    fun `layers identify current user and preserve each route`() {
        val current = UserProfile(
            uid = "samuel",
            name = "Samuel",
            connected = true,
            latitude = 4.6,
            longitude = -74.1,
            route = listOf(RoutePoint(4.6, -74.1, 1L)),
        )
        val other = UserProfile(
            uid = "diego",
            name = "Diego",
            connected = true,
            latitude = 4.7,
            longitude = -74.0,
        )

        val layers = MapRenderLogic.layers(listOf(other, current), "samuel")

        assertEquals("samuel", layers.first().profile.uid)
        assertTrue(layers.first().isCurrentUser)
        assertEquals(1, layers.first().route.size)
        assertFalse(layers.last().isCurrentUser)
    }

    @Test
    fun `blank names use question mark as marker initial`() {
        assertEquals("?", MapRenderLogic.markerInitial(" "))
    }
}

