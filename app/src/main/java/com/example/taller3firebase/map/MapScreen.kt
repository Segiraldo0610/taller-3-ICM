package com.example.taller3firebase.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.taller3firebase.R
import com.example.taller3firebase.tracking.MapUiState
import com.example.taller3firebase.tracking.TrackingMessage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: MapUiState,
    onTrackingChange: (Boolean, Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val layers = MapRenderLogic.layers(state.connectedUsers, state.currentUserId)
    val cameraPositionState = rememberCameraPositionState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onTrackingChange(granted, granted)
    }

    val currentLayer = layers.firstOrNull(MapUserLayer::isCurrentUser)
    LaunchedEffect(currentLayer?.profile?.latitude, currentLayer?.profile?.longitude) {
        val latitude = currentLayer?.profile?.latitude ?: return@LaunchedEffect
        val longitude = currentLayer.profile.longitude ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f),
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.map_title))
                        Text(
                            text = stringResource(if (state.isConnected) R.string.connected else R.string.disconnected),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isConnected) Color(0xFF16803A) else MaterialTheme.colorScheme.error,
                        )
                    }
                },
                actions = {
                    Switch(
                        checked = state.isConnected,
                        enabled = !state.isUpdating,
                        onCheckedChange = { enabled ->
                            val granted = hasLocationPermission(context)
                            if (enabled && !granted) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            } else {
                                onTrackingChange(enabled, granted)
                            }
                        },
                    )
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
            ) {
                layers.forEach { layer ->
                    val latitude = layer.profile.latitude ?: return@forEach
                    val longitude = layer.profile.longitude ?: return@forEach
                    MarkerComposable(
                        keys = arrayOf<Any>(layer.profile.photoUrl, layer.profile.name, latitude, longitude),
                        state = rememberUpdatedMarkerState(LatLng(latitude, longitude)),
                        title = layer.profile.name,
                    ) {
                        ProfilePhotoMarker(
                            photoUrl = layer.profile.photoUrl,
                            name = layer.profile.name,
                            isCurrentUser = layer.isCurrentUser,
                        )
                    }
                    if (layer.route.size >= 2) {
                        Polyline(
                            points = layer.route.map { LatLng(it.latitude, it.longitude) },
                            color = if (layer.isCurrentUser) MaterialTheme.colorScheme.primary else Color(0xFFE8590C),
                            width = if (layer.isCurrentUser) 12f else 8f,
                        )
                    }
                }
            }

            state.message?.let { message ->
                Text(
                    text = stringResource(message.stringRes()),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoMarker(
    modifier: Modifier = Modifier,
    photoUrl: String,
    name: String,
    isCurrentUser: Boolean,
) {
    Box(
        modifier = modifier
            .size(if (isCurrentUser) 64.dp else 54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = if (isCurrentUser) 4.dp else 3.dp,
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color(0xFFE8590C),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = MapRenderLogic.markerInitial(name),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape),
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun TrackingMessage.stringRes(): Int = when (this) {
    TrackingMessage.LOCATION_PERMISSION_REQUIRED -> R.string.location_permission_required
    TrackingMessage.LOCATION_UNAVAILABLE -> R.string.location_unavailable
    TrackingMessage.DATABASE_ERROR -> R.string.database_error
}

