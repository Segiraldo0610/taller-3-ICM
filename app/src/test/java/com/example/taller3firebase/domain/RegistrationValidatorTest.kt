package com.example.taller3firebase.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationValidatorTest {
    @Test
    fun `rejects blank identification before Firebase is called`() {
        val input = RegistrationInput(
            name = "Samuel",
            identification = " ",
            email = "samuel@example.com",
            password = "123456",
            confirmPassword = "123456",
            phone = "3001234567",
            hasProfilePhoto = true,
        )

        assertEquals(RegistrationError.IDENTIFICATION_REQUIRED, RegistrationValidator.validate(input))
    }

    @Test
    fun `accepts a complete registration`() {
        val input = RegistrationInput(
            name = "Samuel",
            identification = "101010",
            email = "samuel@example.com",
            password = "123456",
            confirmPassword = "123456",
            phone = "3001234567",
            hasProfilePhoto = true,
        )

        assertEquals(null, RegistrationValidator.validate(input))
    }

    @Test
    fun `route history keeps only the most recent five hundred points`() {
        val points = (0..500).map { RoutePoint(it.toDouble(), (-it).toDouble(), it.toLong()) }

        val limited = RouteHistory.limit(points)

        assertEquals(500, limited.size)
        assertTrue(limited.first().timestamp == 1L)
        assertTrue(limited.last().timestamp == 500L)
    }
}

