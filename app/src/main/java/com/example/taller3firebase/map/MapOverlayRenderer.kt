package com.example.taller3firebase.map

import android.graphics.Bitmap
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline

internal class MapOverlayRenderer(
    private val map: MapView,
) {
    private data class MarkerIconSignature(
        val name: String,
        val photoUrl: String,
        val photoIdentity: Int,
        val isCurrentUser: Boolean,
        val currentUserColor: Int,
        val otherUserColor: Int,
        val backgroundColor: Int,
        val textColor: Int,
    )

    private val markers = mutableMapOf<String, Marker>()
    private val routes = mutableMapOf<String, Polyline>()
    private val routePoints = mutableMapOf<String, List<com.example.taller3firebase.domain.RoutePoint>>()
    private val iconSignatures = mutableMapOf<String, MarkerIconSignature>()
    private val copyrightOverlay = CopyrightOverlay(map.context)
    private var overlayOrder: List<Overlay> = emptyList()

    fun render(
        layers: List<MapUserLayer>,
        profilePhotos: Map<String, Bitmap>,
        currentUserColor: Int,
        otherUserColor: Int,
        backgroundColor: Int,
        textColor: Int,
    ) {
        val desiredIds = layers.mapTo(mutableSetOf()) { it.profile.uid }
        markers.keys.filterNot(desiredIds::contains).forEach(::removeMarker)
        routes.keys.filterNot(desiredIds::contains).forEach(::removeRoute)

        val renderLayers = layers.asReversed()
        renderLayers.forEach { layer ->
            updateRoute(layer, currentUserColor, otherUserColor)
            updateMarker(
                layer = layer,
                photo = profilePhotos[layer.profile.photoUrl],
                currentUserColor = currentUserColor,
                otherUserColor = otherUserColor,
                backgroundColor = backgroundColor,
                textColor = textColor,
            )
        }

        val desiredOrder = buildList {
            renderLayers.mapNotNullTo(this) { routes[it.profile.uid] }
            renderLayers.mapNotNullTo(this) { markers[it.profile.uid] }
            add(copyrightOverlay)
        }
        if (desiredOrder != overlayOrder) {
            map.overlays.clear()
            map.overlays.addAll(desiredOrder)
            overlayOrder = desiredOrder
        }
        map.invalidate()
    }

    private fun updateRoute(layer: MapUserLayer, currentUserColor: Int, otherUserColor: Int) {
        val uid = layer.profile.uid
        if (layer.route.size < 2) {
            removeRoute(uid)
            return
        }
        val route = routes.getOrPut(uid) { Polyline(map) }
        route.outlinePaint.color = if (layer.isCurrentUser) currentUserColor else otherUserColor
        route.outlinePaint.strokeWidth = if (layer.isCurrentUser) 12f else 8f
        if (routePoints[uid] != layer.route) {
            route.setPoints(layer.route.map { point -> GeoPoint(point.latitude, point.longitude) })
            routePoints[uid] = layer.route
        }
    }

    private fun updateMarker(
        layer: MapUserLayer,
        photo: Bitmap?,
        currentUserColor: Int,
        otherUserColor: Int,
        backgroundColor: Int,
        textColor: Int,
    ) {
        val profile = layer.profile
        val latitude = profile.latitude ?: return
        val longitude = profile.longitude ?: return
        val marker = markers.getOrPut(profile.uid) {
            Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setOnMarkerClickListener { selectedMarker, currentMap ->
                    selectedMarker.showInfoWindow()
                    currentMap.controller.animateTo(selectedMarker.position)
                    true
                }
            }
        }
        marker.position = GeoPoint(latitude, longitude)
        marker.title = profile.name

        val signature = MarkerIconSignature(
            name = profile.name,
            photoUrl = profile.photoUrl,
            photoIdentity = System.identityHashCode(photo),
            isCurrentUser = layer.isCurrentUser,
            currentUserColor = currentUserColor,
            otherUserColor = otherUserColor,
            backgroundColor = backgroundColor,
            textColor = textColor,
        )
        if (iconSignatures[profile.uid] != signature) {
            marker.icon = ProfileMarkerIconFactory.create(
                context = map.context,
                photo = photo,
                initial = MapRenderLogic.markerInitial(profile.name),
                isCurrentUser = layer.isCurrentUser,
                currentUserColor = currentUserColor,
                otherUserColor = otherUserColor,
                backgroundColor = backgroundColor,
                textColor = textColor,
            )
            iconSignatures[profile.uid] = signature
        }
    }

    private fun removeMarker(uid: String) {
        iconSignatures.remove(uid)
        markers.remove(uid)?.let { marker ->
            map.overlays.remove(marker)
            marker.closeInfoWindow()
            marker.onDetach(map)
        }
    }

    private fun removeRoute(uid: String) {
        routePoints.remove(uid)
        routes.remove(uid)?.let { route ->
            map.overlays.remove(route)
            route.onDetach(map)
        }
    }
}
