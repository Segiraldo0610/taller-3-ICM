package com.example.taller3firebase.data

import com.example.taller3firebase.domain.RouteHistory
import com.example.taller3firebase.domain.RoutePoint
import com.example.taller3firebase.domain.UserProfile
import com.example.taller3firebase.tracking.TrackingLogic
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

interface TrackingRepository {
    fun observeConnectedUsers(
        onUsers: (List<UserProfile>) -> Unit,
        onError: (Throwable) -> Unit,
    ): AutoCloseable

    suspend fun startTracking(uid: String)
    suspend fun updateLocation(uid: String, point: RoutePoint)
    suspend fun stopTracking(uid: String)
}

class FirebaseTrackingRepository(
    private val firestore: FirebaseFirestore,
) : TrackingRepository {
    private val users = firestore.collection(FirebaseProfileRepository.USERS_COLLECTION)

    override fun observeConnectedUsers(
        onUsers: (List<UserProfile>) -> Unit,
        onError: (Throwable) -> Unit,
    ): AutoCloseable {
        val registration = users
            .whereEqualTo("connected", true)
            .limit(TrackingLogic.MAX_VISIBLE_USERS.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUsers(snapshot?.documents.orEmpty().mapNotNull(DocumentSnapshot::toUserProfile))
                }
            }
        return AutoCloseable { registration.remove() }
    }

    override suspend fun startTracking(uid: String) {
        users.document(uid).set(
            mapOf("connected" to true, "route" to emptyList<Map<String, Any>>()),
            SetOptions.merge(),
        ).awaitResult()
    }

    override suspend fun updateLocation(uid: String, point: RoutePoint) {
        val reference = users.document(uid)
        firestore.runTransaction { transaction ->
            val profile = transaction.get(reference).toUserProfile()
                ?: UserProfile(uid = uid)
            val updatedRoute = RouteHistory.limit(profile.route + point)
            transaction.set(
                reference,
                mapOf(
                    "uid" to uid,
                    "latitude" to point.latitude,
                    "longitude" to point.longitude,
                    "connected" to true,
                    "route" to updatedRoute.map(RoutePoint::toMap),
                ),
                SetOptions.merge(),
            )
        }.awaitResult()
    }

    override suspend fun stopTracking(uid: String) {
        users.document(uid).set(
            mapOf("connected" to false, "route" to emptyList<Map<String, Any>>()),
            SetOptions.merge(),
        ).awaitResult()
    }
}

private fun RoutePoint.toMap(): Map<String, Any> = mapOf(
    "latitude" to latitude,
    "longitude" to longitude,
    "timestamp" to timestamp,
)

private fun DocumentSnapshot.toUserProfile(): UserProfile? {
    val uid = getString("uid") ?: id.takeIf(String::isNotBlank) ?: return null
    val route = (get("route") as? List<*>)
        .orEmpty()
        .mapNotNull { value ->
            val point = value as? Map<*, *> ?: return@mapNotNull null
            val latitude = (point["latitude"] as? Number)?.toDouble() ?: return@mapNotNull null
            val longitude = (point["longitude"] as? Number)?.toDouble() ?: return@mapNotNull null
            val timestamp = (point["timestamp"] as? Number)?.toLong() ?: 0L
            RoutePoint(latitude, longitude, timestamp)
        }
    return UserProfile(
        uid = uid,
        name = getString("name").orEmpty(),
        identification = getString("identification").orEmpty(),
        email = getString("email").orEmpty(),
        phone = getString("phone").orEmpty(),
        photoUrl = getString("photoUrl").orEmpty(),
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        connected = getBoolean("connected") ?: false,
        route = route,
    )
}

