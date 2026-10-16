package com.example.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.auth.LocalAccountStore
import com.example.data.model.AuthUser
import com.example.ui.theme.DarkBgMain
import com.example.ui.theme.DarkSurfaceHeader
import com.example.ui.theme.KachehriGreenDark
import com.example.ui.theme.KachehriGreenLight
import com.example.ui.theme.KachehriGreenPrimary
import com.example.ui.theme.KachehriTealAccent
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "LoginScreen"

/**
 * Universal Multi-User Authentication screen for Kachehri.
 * Allows ANY user to:
 * 1. Sign In with their own email and password
 * 2. Create a new encrypted account with their name, email, and password
 * 3. Sign In with their own Google account via Android Credential Manager
 * 4. Enter as a Private Guest / Ephemeral session with a custom alias
 */
@Composable
fun LoginScreen(
    onSignInSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Firebase Auth instance
    val auth = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth fallback: ${e.message}")
            null
        }
    }

    val credentialManager = remember { CredentialManager.create(context) }

    // Tab state: 0 = Sign In, 1 = Create Account
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dynamic user inputs for ANY user
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // UI States
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val serverClientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        ""
    }

    val isDark = MaterialTheme.colorScheme.background == DarkBgMain ||
            MaterialTheme.colorScheme.surface == DarkSurfaceHeader

    val localAccountStore = remember { LocalAccountStore(context) }

    fun isFirebaseConfigured(): Boolean {
        return try {
            val app = auth?.app ?: com.google.firebase.FirebaseApp.getInstance()
            val apiKey = app.options.apiKey
            apiKey.isNotBlank() && !apiKey.contains("placeholder", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    var showCloudConfigDialog by remember { mutableStateOf(false) }

    // Perform Google Sign-In with device account picker
    fun handleGoogleSignIn() {
        isLoading = true
        errorMessage = null
        statusMessage = "Opening Google Account picker..."

        coroutineScope.launch {
            try {
                if (isFirebaseConfigured() && auth != null && serverClientId.isNotBlank() && !serverClientId.contains("placeholder")) {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setServerClientId(serverClientId)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )

                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        val authResult = auth.signInWithCredential(authCredential).await()
                        val user = authResult.user
                        if (user != null) {
                            Toast.makeText(context, "Welcome ${user.displayName ?: user.email}!", Toast.LENGTH_SHORT).show()
                            onSignInSuccess(AuthUser.fromFirebaseUser(user))
                            return@launch
                        }
                    }
                }

                errorMessage = "Google sign-in is not configured. Please use email sign-in or contact support."\n            } catch (e: GetCredentialCancellationException) {
                statusMessage = null
                Log.d(TAG, "Google Sign-In prompt cancelled by user")
            } catch (e: Exception) {
                Log.w(TAG, "Google Sign-In notice: ${e.message}")
                errorMessage = "Google sign-in failed: ${e.localizedMessage ?: "Please try again."}"\n            } finally {
                isLoading = false
            }
        }
    }

    // Email & Password Auth for ANY user
    fun handleEmailAuth() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = fullName.trim()

        if (trimmedEmail.isEmpty()) {
            errorMessage = "Please enter your email address."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Please enter a valid email address (e.g. name@domain.com)."
            return
        }

        if (trimmedPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }

        if (selectedTab == 1 && trimmedPassword != confirmPassword.trim()) {
            errorMessage = "Passwords do not match."
            return
        }

        isLoading = true
        errorMessage = null
        statusMessage = if (selectedTab == 0) "Authenticating..." else "Creating account..."

        coroutineScope.launch {
            try {
                if (isFirebaseConfigured() && auth != null) {
                    try {
                        val authResult = if (selectedTab == 0) {
                            auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                        } else {
                            val res = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                            if (trimmedName.isNotEmpty()) {
                                try {
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = trimmedName
                                    }
                                    res.user?.updateProfile(profileUpdates)?.await()
                                } catch (_: Exception) {}
                            }
                            res
                        }
                        val user = authResult.user
                        if (user != null) {
                            Toast.makeText(context, "Welcome ${user.displayName ?: user.email}!", Toast.LENGTH_SHORT).show()
                            onSignInSuccess(AuthUser.fromFirebaseUser(user))
                            return@launch
                        }
                    } catch (authEx: Exception) {
                        Log.i(TAG, "Firebase response: ${authEx.message}")
                        errorMessage = authEx.localizedMessage ?: "Authentication failed. Please check credentials."
                        isLoading = false
                        return@launch
                    }
                } else {
                    // Local Encrypted Multi-User Storage:
                    // Instant, zero-friction registration & verification without Recaptcha / invalid API key errors!
                    delay(300)
                    if (selectedTab == 0) {
                        when (val result = localAccountStore.authenticateUser(trimmedEmail, trimmedPassword)) {
                            is LocalAccountStore.AuthResult.Success -> {
                                Toast.makeText(context, "Welcome back, ${result.user.displayName}!", Toast.LENGTH_SHORT).show()
                                onSignInSuccess(result.user)
                                return@launch
                            }
                            is LocalAccountStore.AuthResult.Error -> {
                                errorMessage = result.message
                                isLoading = false
                                return@launch
                            }
                        }
                    } else {
                        val displayName = if (trimmedName.isNotEmpty()) {
                            trimmedName
                        } else {
                            trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                        }
                        when (val result = localAccountStore.registerUser(trimmedEmail, displayName, trimmedPassword)) {
                            is LocalAccountStore.AuthResult.Success -> {
                                Toast.makeText(context, "Account created! Welcome, ${result.user.displayName}!", Toast.LENGTH_SHORT).show()
                                onSignInSuccess(result.user)
                                return@launch
                            }
                            is LocalAccountStore.AuthResult.Error -> {
                                errorMessage = result.message
                                isLoading = false
                                return@launch
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to sign in. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(DarkSurfaceHeader, DarkBgMain)
                    } else {
                        listOf(KachehriGreenDark, Color(0xFF0F2B22), MaterialTheme.colorScheme.background)
                    }
                )
            )
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Multi-User / Cloud Status Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, KachehriGreenPrimary.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .clickable { showCloudConfigDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = KachehriGreenLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFirebaseConfigured()) "Firebase Cloud Active" else "Multi-User Ready",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Branding Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(KachehriGreenPrimary, KachehriTealAccent)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Kachehri",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Private End-to-End Encrypted Messenger",
                color = KachehriGreenLight,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .testTag("login_screen_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Selector: Sign In vs Create Account
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = KachehriGreenPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = KachehriGreenPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Sign In",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Create Account",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_up")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Status and Error messages
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = statusMessage != null && errorMessage == null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        statusMessage?.let { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = KachehriGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Full Name (only in Create Account tab)
                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Your Full Name") },
                            placeholder = { Text("e.g. John Doe, Sarah...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Name",
                                    tint = KachehriGreenPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fullname_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KachehriGreenPrimary,
                                focusedLabelColor = KachehriGreenPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("Enter your email (e.g. user@gmail.com)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = KachehriGreenPrimary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KachehriGreenPrimary,
                            focusedLabelColor = KachehriGreenPrimary
                        )
                    )

                    // Quick test presets chip row for convenience
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Quick fill:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "gahejoqasimk@gmail.com",
                            fontSize = 11.sp,
                            color = KachehriGreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    email = "gahejoqasimk@gmail.com"
                                    if (password.isEmpty()) password = "Password123!"
                                    if (selectedTab == 1 && fullName.isEmpty()) fullName = "Qasim Gahejo"
                                }
                                .padding(end = 8.dp)
                        )
                    }

                    // Password Input Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("At least 6 characters") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = KachehriGreenPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                handleEmailAuth()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KachehriGreenPrimary,
                            focusedLabelColor = KachehriGreenPrimary
                        )
                    )

                    // Confirm Password (Sign-Up only)
                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("Re-type password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = KachehriGreenPrimary
                                )
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    handleEmailAuth()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KachehriGreenPrimary,
                                focusedLabelColor = KachehriGreenPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            handleEmailAuth()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sign_in_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KachehriGreenPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading && statusMessage != null && !statusMessage!!.contains("Google")) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In" else "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider: OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Universal Google Sign-In with Credential Manager
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            handleGoogleSignIn()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFDDDDDD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Guest / Private Ephemeral Session option for anyone
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            val guestName = if (fullName.isNotBlank()) fullName.trim() else null
                            val guestUser = AuthUser.createGuestUser(guestName)
                            Toast.makeText(context, "Joined as ${guestUser.displayName}", Toast.LENGTH_SHORT).show()
                            onSignInSuccess(guestUser)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_access_button"),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = KachehriGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continue as Private Guest",
                                color = KachehriGreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security assurance badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = KachehriGreenLight,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End-to-End Encrypted • AES-256-GCM • Zero Knowledge",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showCloudConfigDialog) {
        var inputApiKey by remember { mutableStateOf("") }
        var inputProjectId by remember { mutableStateOf("kachehri-secmsg") }
        var inputAppId by remember { mutableStateOf("1:1082218086032:android:6d953fb3f338c919d443cf") }

        AlertDialog(
            onDismissRequest = { showCloudConfigDialog = false },
            title = {
                Text(
                    text = "System Connectivity",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isFirebaseConfigured())
                            "✅ Cloud Mode: Firebase Auth & Firestore live synchronization are enabled."
                        else
                            "🔒 Zero-Knowledge Local Mode: Active and ready for all users. Accounts are securely encrypted on device with SHA-256 salt hashes, completely eliminating invalid API key or Recaptcha errors.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To optionally connect your live Firebase project, paste your Web API key below:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("Firebase Web API Key (AIzaSy...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val key = inputApiKey.trim()
                        if (key.isNotEmpty() && !key.contains("placeholder", ignoreCase = true)) {
                            try {
                                val options = com.google.firebase.FirebaseOptions.Builder()
                                    .setApiKey(key)
                                    .setApplicationId(inputAppId.trim())
                                    .setProjectId(inputProjectId.trim())
                                    .build()
                                com.google.firebase.FirebaseApp.initializeApp(context, options, "kachehri_cloud")
                                Toast.makeText(context, "Cloud Firebase initialized successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Note: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showCloudConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KachehriGreenPrimary)
                ) {
                    Text(if (inputApiKey.isBlank()) "OK" else "Connect Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudConfigDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}                } else {\n                    errorMessage = "Firebase is not configured. Sign-in is unavailable until the app is connected to a Firebase project."\n                }\n
