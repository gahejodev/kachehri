package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.data.model.AuthUser
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages locally registered and authenticated user accounts.
 * Ensures that ANY user can sign up with their name, email, and password,
 * and securely sign in with validation — completely eliminating invalid API key / Recaptcha errors
 * when a cloud Firebase API key is not yet set.
 */
class LocalAccountStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kachehri_local_accounts_v2", Context.MODE_PRIVATE)

    sealed class AuthResult {
        data class Success(val user: AuthUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    init {
        // Pre-seed default administrator / owner account for smooth zero-configuration access
        if (!hasAccount("gahejoqasimk@gmail.com")) {
            registerUser(
                email = "gahejoqasimk@gmail.com",
                displayName = "Qasim Gahejo",
                password = "Password123!"
            )
        }
    }

    fun hasAccount(email: String): Boolean {
        val key = normalizeEmail(email)
        return prefs.contains(key)
    }

    /**
     * Registers a new account.
     */
    fun registerUser(
        email: String,
        displayName: String,
        password: String
    ): AuthResult {
        val normalized = normalizeEmail(email)
        if (prefs.contains(normalized)) {
            return AuthResult.Error("An account with this email already exists. Please sign in.")
        }

        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)
        val uid = "usr_" + normalized.replace(Regex("[^a-zA-Z0-9]"), "").take(14) + "_" + (System.currentTimeMillis() % 10000)

        val json = JSONObject().apply {
            put("email", normalized)
            put("displayName", displayName)
            put("salt", salt)
            put("passwordHash", passwordHash)
            put("uid", uid)
            put("createdAt", System.currentTimeMillis())
        }

        prefs.edit().putString(normalized, json.toString()).apply()

        val authUser = AuthUser(
            uid = uid,
            email = normalized,
            displayName = displayName,
            isAnonymous = false,
            isLocalDemo = false
        )
        return AuthResult.Success(authUser)
    }

    /**
     * Verifies credentials and signs the user in.
     */
    fun authenticateUser(
        email: String,
        password: String
    ): AuthResult {
        val normalized = normalizeEmail(email)
        val accountData = prefs.getString(normalized, null)

        if (accountData == null) {
            // If the account does not exist yet, allow auto-creation if password meets minimum length
            // or provide clear feedback
            return AuthResult.Error("Account not found for '$email'. Please switch to 'Create Account' to register.")
        }

        return try {
            val json = JSONObject(accountData)
            val salt = json.getString("salt")
            val storedHash = json.getString("passwordHash")
            val computedHash = hashPassword(password, salt)

            if (storedHash == computedHash) {
                val displayName = json.optString("displayName", normalized.substringBefore("@").replaceFirstChar { it.uppercase() })
                val uid = json.optString("uid", "usr_" + normalized.replace(Regex("[^a-zA-Z0-9]"), "").take(14))
                val authUser = AuthUser(
                    uid = uid,
                    email = normalized,
                    displayName = displayName,
                    isAnonymous = false,
                    isLocalDemo = false
                )
                AuthResult.Success(authUser)
            } else {
                AuthResult.Error("Incorrect password. Please try again.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Authentication error: ${e.localizedMessage}")
        }
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
