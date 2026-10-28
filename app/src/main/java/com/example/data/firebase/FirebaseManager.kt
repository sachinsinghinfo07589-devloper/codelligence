package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.Listing
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    // Firestore instance with custom named database ID from firebase_applet_config.xml
    val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
    }

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserState.value = firebaseAuth.currentUser
        }
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun getSignedInUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return User(
            id = fbUser.uid,
            name = fbUser.displayName ?: "Google User",
            email = fbUser.email ?: "user@google.com",
            role = UserRole.BUSINESS_OWNER,
            avatarUrl = fbUser.photoUrl?.toString()
        )
    }

    // Interactive Google Sign-In via Credential Manager
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            val err = "default_web_client_id resource not found."
            _authError.value = err
            onError(err)
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    _currentUserState.value = authResult.user

                    // Save user to Firestore
                    authResult.user?.let { u ->
                        saveUserToFirestore(u)
                    }

                    onSuccess()
                } else {
                    onError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("FirebaseManager", "Google Sign-In cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e("FirebaseManager", "Google Sign-In failed", e)
                _authError.value = e.localizedMessage
                onError(e.localizedMessage ?: "Sign-in failed.")
            }
        }
    }

    // Silent Auto Sign-In on launch
    fun attemptSilentSignIn(scope: CoroutineScope) {
        if (auth.currentUser != null) return

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                }
            } catch (_: Exception) {
                // Silent check fails gracefully
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("FirebaseManager", "Clear credential error", e)
            } finally {
                _currentUserState.value = null
                onComplete()
            }
        }
    }

    private suspend fun saveUserToFirestore(user: FirebaseUser) = withContext(Dispatchers.IO) {
        try {
            val userMap = mapOf(
                "id" to user.uid,
                "name" to (user.displayName ?: "User"),
                "email" to (user.email ?: ""),
                "role" to "USER",
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.uid).set(userMap).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving user to Firestore", e)
        }
    }

    // Push local listing to Firestore cloud database
    suspend fun syncListingToFirestore(listing: Listing) = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "id" to listing.id,
                "businessId" to listing.businessId,
                "businessName" to listing.businessName,
                "title" to listing.title,
                "description" to listing.description,
                "category" to listing.category,
                "listingType" to listing.listingType.name,
                "destinationUrl" to (listing.destinationUrl ?: ""),
                "city" to (listing.city ?: ""),
                "state" to (listing.state ?: ""),
                "isOnlineAvailable" to listing.isOnlineAvailable,
                "contactEmail" to (listing.contactEmail ?: ""),
                "contactPhone" to (listing.contactPhone ?: ""),
                "status" to listing.status.name,
                "validityDays" to listing.validityDays,
                "createdAt" to listing.createdAt,
                "expiresAt" to listing.expiresAt
            )
            firestore.collection("listings").document(listing.id).set(map).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Failed to sync listing to Firestore", e)
        }
    }

    // Observe listings from Firestore cloud database
    fun attachListingsListener(onListingsUpdated: (List<Listing>) -> Unit) {
        try {
            firestore.collection("listings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: return@mapNotNull null
                            val desc = doc.getString("description") ?: ""
                            val bizName = doc.getString("businessName") ?: "Business"
                            val cat = doc.getString("category") ?: "General"
                            val typeStr = doc.getString("listingType") ?: "SERVICE"
                            val statusStr = doc.getString("status") ?: "APPROVED"
                            val type = try { ListingType.valueOf(typeStr) } catch (_: Exception) { ListingType.SERVICE }
                            val status = try { ListingStatus.valueOf(statusStr) } catch (_: Exception) { ListingStatus.APPROVED }
                            Listing(
                                id = id,
                                businessId = doc.getString("businessId") ?: "biz_01",
                                businessName = bizName,
                                title = title,
                                description = desc,
                                category = cat,
                                listingType = type,
                                destinationUrl = doc.getString("destinationUrl"),
                                city = doc.getString("city"),
                                state = doc.getString("state"),
                                isOnlineAvailable = doc.getBoolean("isOnlineAvailable") ?: true,
                                contactEmail = doc.getString("contactEmail"),
                                contactPhone = doc.getString("contactPhone"),
                                status = status,
                                validityDays = doc.getLong("validityDays")?.toInt() ?: 60,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                expiresAt = doc.getLong("expiresAt") ?: (System.currentTimeMillis() + 60L * 24 * 3600 * 1000)
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (list.isNotEmpty()) {
                        onListingsUpdated(list)
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error attaching snapshot listener", e)
        }
    }

    suspend fun testFirestoreConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            val snapshot = firestore.collection("listings").limit(5).get().await()
            Result.success("Firestore connected! ($dbId) - ${snapshot.size()} document(s) verified.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
