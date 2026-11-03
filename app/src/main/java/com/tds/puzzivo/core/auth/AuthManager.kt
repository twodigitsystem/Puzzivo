package com.tds.puzzivo.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.tds.puzzivo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    private val credentialManager = CredentialManager.create(context)

    val authStateFlow: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun getUserId(): String? = firebaseAuth.currentUser?.uid

    fun getUserDisplayName(): String? = firebaseAuth.currentUser?.displayName

    fun getUserEmail(): String? = firebaseAuth.currentUser?.email

    fun getUserPhotoUrl(): String? = firebaseAuth.currentUser?.photoUrl?.toString()


    /**
     * Authenticate with Google using Android Credential Manager API.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            handleCredentialResult(result.credential)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by user")
            Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.d(TAG, "No Google credential is available")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Google", e)
            Result.failure(e)
        }
    }

    private suspend fun handleCredentialResult(credential: Credential): Result<FirebaseUser> {
        return if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Google ID token", e)
                Result.failure(e)
            }
        } else {
            val message = "Unexpected credential type: ${credential.type}"
            Log.w(TAG, message)
            Result.failure(IllegalArgumentException(message))
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()
            val user = authResult.user ?: return Result.failure(Exception("Sign-in succeeded but user is null"))
            Log.d(TAG, "Firebase auth successful: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user ?: return Result.failure(Exception("Anonymous sign-in failed"))
            Log.d(TAG, "Anonymous sign-in successful: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed", e)
            Result.failure(e)
        }
    }

    /**
     * Link an existing anonymous account to Google credentials.
     */
    suspend fun linkAnonymousToGoogle(activityContext: Context): Result<FirebaseUser> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || !currentUser.isAnonymous) {
            return signInWithGoogle(activityContext)
        }

        return try {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                try {
                    val authResult = currentUser.linkWithCredential(authCredential).await()
                    val user = authResult.user ?: return Result.failure(Exception("Account link failed"))
                    Log.d(TAG, "Account linked successfully: ${user.uid}")
                    Result.success(user)
                } catch (collision: FirebaseAuthUserCollisionException) {
                    Log.w(TAG, "Google account already linked to another user, signing in directly: ${collision.message}")
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                }
            } else {
                Result.failure(IllegalArgumentException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google linking was cancelled by user")
            Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.d(TAG, "No Google credential is available for account linking")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error linking anonymous account to Google", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            firebaseAuth.signOut()
            Log.d(TAG, "Sign out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to delete"))

            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            user.delete().await()
            Log.d(TAG, "Account deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Result.failure(e)
        }
    }

    /**
     * Ensure user is signed in (anonymously if needed)
     */
    suspend fun ensureSignedIn(): Result<FirebaseUser> {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            Result.success(currentUser)
        } else {
            signInAnonymously()
        }
    }

    /**
     * Flow of current user
     */
    val currentUser: StateFlow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        trySend(firebaseAuth.currentUser)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = firebaseAuth.currentUser
    )

    companion object {
        private const val TAG = "AuthManager"
    }
}
