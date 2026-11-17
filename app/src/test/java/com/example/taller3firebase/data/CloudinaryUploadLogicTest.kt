package com.example.taller3firebase.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CloudinaryUploadLogicTest {

    @Test
    fun `configuration rejects blank cloud name`() {
        val settings = CloudinarySettings(
            cloudName = "",
            uploadPreset = "taller3_unsigned",
        )

        assertThrows(IllegalArgumentException::class.java) {
            settings.requireConfigured()
        }
    }

    @Test
    fun `configuration rejects blank upload preset`() {
        val settings = CloudinarySettings(
            cloudName = "samuel-cloud",
            uploadPreset = "",
        )

        assertThrows(IllegalArgumentException::class.java) {
            settings.requireConfigured()
        }
    }

    @Test
    fun `configuration accepts cloud name and unsigned preset`() {
        val settings = CloudinarySettings(
            cloudName = "samuel-cloud",
            uploadPreset = "taller3_unsigned",
        )

        assertEquals(settings, settings.requireConfigured())
    }

    @Test
    fun `upload response returns secure url`() {
        val result = mapOf<Any, Any>(
            "secure_url" to "https://res.cloudinary.com/samuel/image/upload/profile.jpg",
        )

        assertEquals(
            "https://res.cloudinary.com/samuel/image/upload/profile.jpg",
            CloudinaryUploadResult.secureUrl(result),
        )
    }

    @Test
    fun `upload response rejects missing secure url`() {
        assertThrows(IllegalStateException::class.java) {
            CloudinaryUploadResult.secureUrl(emptyMap<Any, Any>())
        }
    }
}
