package com.example.data.model

import com.google.firebase.auth.FirebaseUser

/**
 * Unified representation of an authenticated user session in Kachehri.
 * Seamlessly wraps Firebase Authentication users and provides seamless authentication
 * for gahejoqasimk@gmail.com.
 */
data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val isLocalDemo: Boolean = false
) {
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser): AuthUser {
            val resolvedName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "User"
            return AuthUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = resolvedName,
                isAnonymous = firebaseUser.isAnonymous,
                isLocalDemo = false
            )
        }

        fun createAuthenticatedUser(
            email: String,
            displayName: String? = null
        ): AuthUser {
            val resolvedName = displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val cleanUid = "usr_" + email.replace(Regex("[^a-zA-Z0-9]"), "").take(16) + "_" + (System.currentTimeMillis() % 100000)
            return AuthUser(
                uid = cleanUid,
                email = email,
                displayName = resolvedName,
                isAnonymous = false,
                isLocalDemo = false
            )
        }

        fun createGuestUser(name: String? = null): AuthUser {
            val randomTag = (1000..9999).random()
            val guestName = name?.takeIf { it.isNotBlank() } ?: "Guest_$randomTag"
            return AuthUser(
                uid = "guest_$randomTag",
                email = null,
                displayName = guestName,
                isAnonymous = true,
                isLocalDemo = false
            )
        }

        fun createDemoUser(email: String = "gahejoqasimk@gmail.com"): AuthUser {
            return createAuthenticatedUser(email = email)
        }
    }
}
