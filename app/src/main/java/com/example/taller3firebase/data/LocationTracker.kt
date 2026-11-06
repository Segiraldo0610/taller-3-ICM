package com.example.taller3firebase.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.taller3firebase.domain.RoutePoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

interface LocationTracker {
    fun start(onLocation: (RoutePoint) -> Unit, onError: (Throwable) -> Unit)
    fun stop()
}

class FusedLocationTracker(
    context: Context,
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
) : LocationTracker {
    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun start(onLocation: (RoutePoint) -> Unit, onError: (Throwable) -> Unit) {
        stop()
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MILLIS,
        ).setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocation(
                        RoutePoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.time,
                        ),
                    )
                }
            }
        }
        callback = locationCallback
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            .addOnFailureListener(onError)
    }

    override fun stop() {
        callback?.let(client::removeLocationUpdates)
        callback = null
    }

    companion object {
        private const val UPDATE_INTERVAL_MILLIS = 3_000L
        private const val MIN_UPDATE_INTERVAL_MILLIS = 2_000L
    }
}

