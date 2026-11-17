package com.example.taller3firebase.data

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.taller3firebase.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface PhotoUploader {
    suspend fun upload(uid: String, photoUri: Uri): String
}

data class CloudinarySettings(
    val cloudName: String,
    val uploadPreset: String,
) {
    fun requireConfigured(): CloudinarySettings {
        require(cloudName.isNotBlank()) { "Cloudinary cloud name is missing" }
        require(uploadPreset.isNotBlank()) { "Cloudinary unsigned upload preset is missing" }
        return this
    }

    companion object {
        fun fromBuildConfig(): CloudinarySettings = CloudinarySettings(
            cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
            uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET,
        )
    }
}

object CloudinaryUploadResult {
    fun secureUrl(resultData: Map<*, *>): String =
        (resultData["secure_url"] as? String)
            ?.takeIf { it.startsWith("https://") }
            ?: error("Cloudinary response did not contain a secure URL")
}

class CloudinaryPhotoUploader(
    context: Context,
    private val settings: CloudinarySettings = CloudinarySettings.fromBuildConfig(),
) : PhotoUploader {
    private val applicationContext = context.applicationContext

    override suspend fun upload(uid: String, photoUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            val configuredSettings = settings.requireConfigured()
            initialize(configuredSettings)

            val requestId = MediaManager.get()
                .upload(photoUri)
                .unsigned(configuredSettings.uploadPreset)
                .option("folder", PROFILE_PHOTOS_FOLDER)
                .option("public_id", uid)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) = Unit

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) = Unit

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        if (!continuation.isActive) return
                        runCatching { CloudinaryUploadResult.secureUrl(resultData) }
                            .onSuccess { url -> continuation.resume(url) }
                            .onFailure { error -> continuation.resumeWithException(error) }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException(error.description))
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException(error.description))
                        }
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                runCatching { MediaManager.get().cancelRequest(requestId) }
            }
        }

    private fun initialize(configuredSettings: CloudinarySettings) {
        synchronized(initializationLock) {
            if (initialized) return
            val config = mapOf(
                "cloud_name" to configuredSettings.cloudName,
                "secure" to true,
            )
            runCatching { MediaManager.init(applicationContext, config) }
                .onFailure { error ->
                    if (error !is IllegalStateException) throw error
                }
            initialized = true
        }
    }

    private companion object {
        const val PROFILE_PHOTOS_FOLDER = "taller3/profile_photos"
        val initializationLock = Any()

        @Volatile
        var initialized = false
    }
}
