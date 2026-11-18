package com.example.taller3firebase.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.BitmapImage
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.example.taller3firebase.R
import com.example.taller3firebase.tracking.MapUiState
import com.example.taller3firebase.tracking.TrackingMessage
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import kotlinx.coroutines.CancellationException

private const val OSM_USER_AGENT = "EnRutaTaller3/1.0 (aplicacion academica Android)"
private const val OSM_PREFERENCES = "osmdroid"
private const val INITIAL_ZOOM = 13.0
private const val TRACKING_ZOOM = 16.0
private const val BOGOTA_LATITUDE = 4.6486
private const val BOGOTA_LONGITUDE = -74.1058
private val OTHER_USER_COLOR = Color(0xFFE8590C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: MapUiState,
    onTrackingChange: (Boolean, Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layers = MapRenderLogic.layers(state.connectedUsers, state.currentUserId)
    val currentCenter = MapRenderLogic.currentUserCenter(layers)
    val profilePhotos = remember { mutableStateMapOf<String, Bitmap>() }
    val currentUserColor = MaterialTheme.colorScheme.primary.toArgb()
    val markerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val markerTextColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val otherUserColor = OTHER_USER_COLOR.toArgb()

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences(OSM_PREFERENCES, Context.MODE_PRIVATE))
            userAgentValue = OSM_USER_AGENT
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setTilesScaledToDpi(true)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(INITIAL_ZOOM)
            controller.setCenter(GeoPoint(BOGOTA_LATITUDE, BOGOTA_LONGITUDE))
        }
    }
    val overlayRenderer = remember(mapView) { MapOverlayRenderer(mapView) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onTrackingChange(granted, granted)
    }

    val photoUrls = layers.map(MapUserLayer::profile)
        .map { it.photoUrl }
        .filter(String::isNotBlank)
        .distinct()
    LaunchedEffect(photoUrls) {
        profilePhotos.keys.toList()
            .filterNot(photoUrls::contains)
            .forEach(profilePhotos::remove)
        photoUrls.forEach { photoUrl ->
            if (profilePhotos[photoUrl] == null) {
                val result = try {
                    SingletonImageLoader.get(context).execute(
                        ImageRequest.Builder(context)
                            .data(photoUrl)
                            .size(160)
                            .allowHardware(false)
                            .build(),
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                } as? SuccessResult
                val bitmap = (result?.image as? BitmapImage)?.bitmap
                if (bitmap != null) profilePhotos[photoUrl] = bitmap
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(currentCenter) {
        currentCenter?.let { center ->
            mapView.controller.setZoom(TRACKING_ZOOM)
            mapView.controller.animateTo(GeoPoint(center.latitude, center.longitude))
        }
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
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                update = {
                    overlayRenderer.render(
                        layers = layers,
                        profilePhotos = profilePhotos,
                        currentUserColor = currentUserColor,
                        otherUserColor = otherUserColor,
                        backgroundColor = markerBackgroundColor,
                        textColor = markerTextColor,
                    )
                },
            )

            state.message?.let { message ->
                Text(
                    text = stringResource(message.stringRes()),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
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
