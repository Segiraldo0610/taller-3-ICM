package com.example.taller3firebase.domain

data class RegistrationInput(
    val name: String,
    val identification: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phone: String,
    val hasProfilePhoto: Boolean,
)

enum class RegistrationError {
    NAME_REQUIRED,
    IDENTIFICATION_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_TOO_SHORT,
    PASSWORDS_DO_NOT_MATCH,
    PHONE_REQUIRED,
    PHOTO_REQUIRED,
}

object RegistrationValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun validate(input: RegistrationInput): RegistrationError? = when {
        input.name.isBlank() -> RegistrationError.NAME_REQUIRED
        input.identification.isBlank() -> RegistrationError.IDENTIFICATION_REQUIRED
        !emailRegex.matches(input.email.trim()) -> RegistrationError.EMAIL_INVALID
        input.password.length < 6 -> RegistrationError.PASSWORD_TOO_SHORT
        input.password != input.confirmPassword -> RegistrationError.PASSWORDS_DO_NOT_MATCH
        input.phone.isBlank() -> RegistrationError.PHONE_REQUIRED
        !input.hasProfilePhoto -> RegistrationError.PHOTO_REQUIRED
        else -> null
    }
}

