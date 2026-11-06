package com.example.taller3firebase.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taller3firebase.data.AuthRepository
import com.example.taller3firebase.data.ProfileRepository
import com.example.taller3firebase.domain.RegistrationError
import com.example.taller3firebase.domain.RegistrationValidator
import com.example.taller3firebase.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun setMode(mode: AuthMode) = _state.update { AuthFormLogic.switchMode(it, mode) }
    fun setName(value: String) = update { copy(name = value, error = null) }
    fun setIdentification(value: String) = update { copy(identification = value, error = null) }
    fun setEmail(value: String) = update { copy(email = value, error = null) }
    fun setPhone(value: String) = update { copy(phone = value, error = null) }
    fun setPassword(value: String) = update { copy(password = value, error = null) }
    fun setConfirmPassword(value: String) = update { copy(confirmPassword = value, error = null) }
    fun setPhotoUri(value: String) = update { copy(photoUri = value, error = null) }

    fun submit() {
        if (_state.value.isLoading) return
        when (_state.value.mode) {
            AuthMode.LOGIN -> login()
            AuthMode.REGISTER -> register()
        }
    }

    private fun login() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = AuthMessage.INVALID_CREDENTIALS) }
            return
        }
        viewModelScope.launch {
            runCatching {
                _state.update { it.copy(isLoading = true, error = null) }
                authRepository.signIn(current.email, current.password)
            }.onFailure {
                _state.update { it.copy(error = AuthMessage.INVALID_CREDENTIALS) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun register() {
        val current = _state.value
        val registrationInput = AuthFormLogic.toRegistrationInput(current)
        val validationError = RegistrationValidator.validate(registrationInput)
        if (validationError != null) {
            _state.update { it.copy(error = validationError.toAuthMessage()) }
            return
        }

        viewModelScope.launch {
            var userCreated = false
            runCatching {
                _state.update { it.copy(isLoading = true, error = null) }
                val uid = authRepository.register(current.email, current.password)
                userCreated = true
                profileRepository.createProfile(
                    profile = UserProfile(
                        uid = uid,
                        name = current.name.trim(),
                        identification = current.identification.trim(),
                        email = current.email.trim(),
                        phone = current.phone.trim(),
                    ),
                    photoUri = Uri.parse(requireNotNull(current.photoUri)),
                )
            }.onFailure {
                if (userCreated) runCatching { authRepository.deleteCurrentUser() }
                _state.update {
                    it.copy(error = if (userCreated) AuthMessage.PROFILE_SAVE_FAILED else AuthMessage.AUTHENTICATION_FAILED)
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun update(block: AuthFormState.() -> AuthFormState) = _state.update { it.block() }
}

private fun RegistrationError.toAuthMessage(): AuthMessage = when (this) {
    RegistrationError.NAME_REQUIRED -> AuthMessage.NAME_REQUIRED
    RegistrationError.IDENTIFICATION_REQUIRED -> AuthMessage.IDENTIFICATION_REQUIRED
    RegistrationError.EMAIL_INVALID -> AuthMessage.EMAIL_INVALID
    RegistrationError.PASSWORD_TOO_SHORT -> AuthMessage.PASSWORD_TOO_SHORT
    RegistrationError.PASSWORDS_DO_NOT_MATCH -> AuthMessage.PASSWORDS_DO_NOT_MATCH
    RegistrationError.PHONE_REQUIRED -> AuthMessage.PHONE_REQUIRED
    RegistrationError.PHOTO_REQUIRED -> AuthMessage.PHOTO_REQUIRED
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(authRepository, profileRepository) as T
}

