package com.example.taller3firebase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taller3firebase.auth.AuthScreen
import com.example.taller3firebase.auth.AuthViewModel
import com.example.taller3firebase.auth.AuthViewModelFactory
import com.example.taller3firebase.di.AppContainer
import com.example.taller3firebase.map.MapScreen
import com.example.taller3firebase.tracking.MapViewModel
import com.example.taller3firebase.tracking.MapViewModelFactory

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    container: AppContainer,
) {
    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(container.authRepository),
    )
    val userId by sessionViewModel.userId.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(if (userId == null) AuthRoute else MapRoute)

    LaunchedEffect(userId) {
        backStack.clear()
        backStack.add(if (userId == null) AuthRoute else MapRoute)
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<AuthRoute> {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(
                        authRepository = container.authRepository,
                        profileRepository = container.profileRepository,
                    ),
                )
                val state by authViewModel.state.collectAsStateWithLifecycle()
                AuthScreen(
                    state = state,
                    onModeChange = authViewModel::setMode,
                    onNameChange = authViewModel::setName,
                    onIdentificationChange = authViewModel::setIdentification,
                    onEmailChange = authViewModel::setEmail,
                    onPhoneChange = authViewModel::setPhone,
                    onPasswordChange = authViewModel::setPassword,
                    onConfirmPasswordChange = authViewModel::setConfirmPassword,
                    onPhotoCaptured = authViewModel::setPhotoUri,
                    onSubmit = authViewModel::submit,
                )
            }

            entry<MapRoute> {
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(
                        authRepository = container.authRepository,
                        trackingRepository = container.trackingRepository,
                        locationTracker = container.locationTracker,
                    ),
                )
                val state by mapViewModel.state.collectAsStateWithLifecycle()
                MapScreen(
                    state = state,
                    onTrackingChange = mapViewModel::setTrackingEnabled,
                    onLogout = mapViewModel::logout,
                )
            }
        },
    )
}

