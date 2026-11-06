package com.example.taller3firebase.auth

import com.example.taller3firebase.domain.RegistrationInput

enum class AuthMode { LOGIN, REGISTER }

enum class AuthMessage {
    NAME_REQUIRED,
    IDENTIFICATION_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    PASSWORD_TOO_SHORT,
    PASSWORDS_DO_NOT_MATCH,
    PHONE_REQUIRED,
    PHOTO_REQUIRED,
    INVALID_CREDENTIALS,
    AUTHENTICATION_FAILED,
    PROFILE_SAVE_FAILED,
}

data class AuthFormState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val identification: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false,
    val error: AuthMessage? = null,
)

object AuthFormLogic {
    fun switchMode(state: AuthFormState, mode: AuthMode): AuthFormState = state.copy(
        mode = mode,
        password = "",
        confirmPassword = "",
        error = null,
    )

    fun toRegistrationInput(state: AuthFormState): RegistrationInput = RegistrationInput(
        name = state.name,
        identification = state.identification,
        email = state.email,
        password = state.password,
        confirmPassword = state.confirmPassword,
        phone = state.phone,
        hasProfilePhoto = state.photoUri != null,
    )
}

