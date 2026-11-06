package com.example.taller3firebase.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taller3firebase.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    private val _userId = MutableStateFlow(authRepository.currentUserId)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val listener = authRepository.observeSession { uid -> _userId.value = uid }

    override fun onCleared() {
        listener.close()
    }
}

class SessionViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SessionViewModel(authRepository) as T
}

