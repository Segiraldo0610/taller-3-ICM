package com.example.taller3firebase.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taller3firebase.data.AuthRepository
import com.example.taller3firebase.data.LocationTracker
import com.example.taller3firebase.data.TrackingRepository
import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TrackingMessage {
    LOCATION_PERMISSION_REQUIRED,
    LOCATION_UNAVAILABLE,
    DATABASE_ERROR,
}

data class MapUiState(
    val currentUserId: String = "",
    val isConnected: Boolean = false,
    val isUpdating: Boolean = false,
    val connectedUsers: List<UserProfile> = emptyList(),
    val message: TrackingMessage? = null,
)

class MapViewModel(
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
    private val locationTracker: LocationTracker,
) : ViewModel() {
    private val uid = authRepository.currentUserId.orEmpty()
    private val _state = MutableStateFlow(MapUiState(currentUserId = uid))
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private val connectedUsersListener = trackingRepository.observeConnectedUsers(
        onUsers = { users ->
            _state.update { it.copy(connectedUsers = TrackingLogic.visibleUsers(users), message = null) }
        },
        onError = {
            _state.update { it.copy(message = TrackingMessage.DATABASE_ERROR) }
        },
    )

    fun setTrackingEnabled(enabled: Boolean, hasLocationPermission: Boolean) {
        if (enabled && !hasLocationPermission) {
            _state.update { it.copy(isConnected = false, message = TrackingMessage.LOCATION_PERMISSION_REQUIRED) }
            return
        }
        if (enabled) startTracking() else stopTracking()
    }

    fun logout() {
        viewModelScope.launch {
            disconnectFromFirestore()
            authRepository.signOut()
        }
    }

    fun disconnect() {
        if (_state.value.isConnected) stopTracking()
    }

    private fun startTracking() {
        if (uid.isBlank() || _state.value.isConnected) return
        viewModelScope.launch {
            runCatching {
                _state.update { it.copy(isUpdating = true, message = null) }
                trackingRepository.startTracking(uid)
                locationTracker.start(::recordLocation) {
                    _state.update { state -> state.copy(message = TrackingMessage.LOCATION_UNAVAILABLE) }
                }
                _state.update { it.copy(isConnected = true) }
            }.onFailure {
                _state.update { it.copy(isConnected = false, message = TrackingMessage.DATABASE_ERROR) }
            }
            _state.update { it.copy(isUpdating = false) }
        }
    }

    private fun stopTracking() {
        locationTracker.stop()
        _state.update { it.copy(isConnected = false, isUpdating = true) }
        viewModelScope.launch {
            disconnectFromFirestore()
            _state.update { it.copy(isUpdating = false) }
        }
    }

    private fun recordLocation(point: RoutePoint) {
        viewModelScope.launch {
            runCatching { trackingRepository.updateLocation(uid, point) }
                .onFailure { _state.update { it.copy(message = TrackingMessage.DATABASE_ERROR) } }
        }
    }

    private suspend fun disconnectFromFirestore() {
        locationTracker.stop()
        if (uid.isNotBlank()) {
            runCatching { trackingRepository.stopTracking(uid) }
                .onFailure { _state.update { it.copy(message = TrackingMessage.DATABASE_ERROR) } }
        }
        _state.update { it.copy(isConnected = false) }
    }

    override fun onCleared() {
        locationTracker.stop()
        connectedUsersListener.close()
        super.onCleared()
    }
}

class MapViewModelFactory(
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
    private val locationTracker: LocationTracker,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MapViewModel(authRepository, trackingRepository, locationTracker) as T
}

