package com.example.taller3firebase.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthFormLogicTest {
    @Test
    fun `switching mode keeps email and clears secret fields`() {
        val state = AuthFormState(
            mode = AuthMode.LOGIN,
            email = "samuel@example.com",
            password = "secret1",
            confirmPassword = "secret1",
            error = AuthMessage.INVALID_CREDENTIALS,
        )

        val updated = AuthFormLogic.switchMode(state, AuthMode.REGISTER)

        assertEquals(AuthMode.REGISTER, updated.mode)
        assertEquals("samuel@example.com", updated.email)
        assertEquals("", updated.password)
        assertEquals("", updated.confirmPassword)
        assertEquals(null, updated.error)
    }

    @Test
    fun `registration input reports whether a profile photo exists`() {
        val state = AuthFormState(
            mode = AuthMode.REGISTER,
            name = "Samuel",
            identification = "1010",
            email = "samuel@example.com",
            phone = "3001234567",
            password = "123456",
            confirmPassword = "123456",
            photoUri = null,
        )

        val input = AuthFormLogic.toRegistrationInput(state)

        assertFalse(input.hasProfilePhoto)
        assertEquals("1010", input.identification)
    }
}

