package com.example.taller3firebase.data

import com.example.taller3firebase.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileDocumentTest {

    @Test
    fun `profile document stores Cloudinary url and initial tracking state`() {
        val profile = UserProfile(
            uid = "user-123",
            name = "Samuel",
            identification = "123456",
            email = "samuel@example.com",
            phone = "3001234567",
        )

        val document = profile.toFirestoreDocument(
            photoUrl = "https://res.cloudinary.com/samuel/image/upload/user-123.jpg",
        )

        assertEquals("user-123", document["uid"])
        assertEquals("Samuel", document["name"])
        assertEquals("123456", document["identification"])
        assertEquals("samuel@example.com", document["email"])
        assertEquals("3001234567", document["phone"])
        assertEquals(
            "https://res.cloudinary.com/samuel/image/upload/user-123.jpg",
            document["photoUrl"],
        )
        assertNull(document["latitude"])
        assertNull(document["longitude"])
        assertFalse(document["connected"] as Boolean)
        assertEquals(emptyList<Map<String, Any>>(), document["route"])
    }
}
