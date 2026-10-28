package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.model.*
import com.example.data.repository.CodelligenceRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: CodelligenceRepository = CodelligenceRepository()
) : ViewModel() {

    var firebaseManager: FirebaseManager? = null
        private set

    val currentUser = repository.currentUser
    val currentBusiness = repository.currentBusiness
    val allListings = repository.listings
    val savedListingIds = repository.savedListingIds
    val reports = repository.reports
    val auditLogs = repository.auditLogs
    val notifications = repository.notifications
    val categories = repository.categories

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    fun initFirebase(context: Context) {
        if (firebaseManager == null) {
            val manager = FirebaseManager(context.applicationContext)
            firebaseManager = manager
            manager.attemptSilentSignIn(viewModelScope)

            viewModelScope.launch {
                manager.currentUserState.collect { fbUser ->
                    _firebaseUser.value = fbUser
                    if (fbUser != null) {
                        repository.updateUser(
                            name = fbUser.displayName ?: "Google User",
                            email = fbUser.email ?: "user@google.com"
                        )
                    }
                }
            }

            // Real-time Firestore sync
            manager.attachListingsListener { cloudListings ->
                repository.mergeCloudListings(cloudListings)
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseManager?.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun signOut(onComplete: () -> Unit) {
        firebaseManager?.signOut(viewModelScope, onComplete)
    }

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow<ListingType?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    // Filtered Approved Listings
    val filteredApprovedListings: StateFlow<List<Listing>> = combine(
        allListings,
        searchQuery,
        selectedCategory,
        selectedType,
        selectedCity
    ) { listings, query, category, type, city ->
        val now = System.currentTimeMillis()
        listings.filter { listing ->
            val isApprovedAndActive = listing.status == ListingStatus.APPROVED && listing.expiresAt > now
            val matchesCategory = category == null || listing.category.equals(category, ignoreCase = true)
            val matchesType = type == null || listing.listingType == type
            val matchesCity = city == null || listing.city?.contains(city, ignoreCase = true) == true || (city == "Online" && listing.isOnlineAvailable)
            val matchesQuery = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true) ||
                    listing.businessName.contains(query, ignoreCase = true) ||
                    listing.tags.any { it.contains(query, ignoreCase = true) }

            isApprovedAndActive && matchesCategory && matchesType && matchesCity && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved Listings
    val savedListings: StateFlow<List<Listing>> = combine(allListings, savedListingIds) { listings, savedIds ->
        listings.filter { savedIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending Listings for Moderation
    val pendingListings: StateFlow<List<Listing>> = allListings.map { listings ->
        listings.filter { it.status == ListingStatus.PENDING_REVIEW }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My Listings
    val myListings: StateFlow<List<Listing>> = combine(allListings, currentBusiness) { listings, biz ->
        val bizId = biz?.id ?: "biz_01"
        listings.filter { it.businessId == bizId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Action handlers
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectType(type: ListingType?) {
        _selectedType.value = if (_selectedType.value == type) null else type
    }

    fun selectCity(city: String?) {
        _selectedCity.value = if (_selectedCity.value == city) null else city
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedType.value = null
        _selectedCity.value = null
    }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            repository.setUserRole(role)
        }
    }

    fun submitListing(
        title: String,
        description: String,
        category: String,
        listingType: ListingType,
        destinationUrl: String?,
        city: String?,
        state: String?,
        isOnlineAvailable: Boolean,
        contactEmail: String?,
        contactPhone: String?
    ): Listing {
        val newListing = repository.submitListing(
            title = title,
            description = description,
            category = category,
            listingType = listingType,
            destinationUrl = destinationUrl,
            city = city,
            state = state,
            isOnlineAvailable = isOnlineAvailable,
            contactEmail = contactEmail,
            contactPhone = contactPhone
        )

        // Sync to Firestore Cloud Database
        viewModelScope.launch {
            firebaseManager?.syncListingToFirestore(newListing)
        }

        return newListing
    }

    fun renewListing(listingId: String) {
        viewModelScope.launch {
            repository.renewListing(listingId)
            repository.getListingById(listingId)?.let {
                firebaseManager?.syncListingToFirestore(it)
            }
        }
    }

    fun approveListing(listingId: String) {
        viewModelScope.launch {
            repository.approveListing(listingId)
            repository.getListingById(listingId)?.let {
                firebaseManager?.syncListingToFirestore(it)
            }
        }
    }

    fun rejectListing(listingId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectListing(listingId, reason)
            repository.getListingById(listingId)?.let {
                firebaseManager?.syncListingToFirestore(it)
            }
        }
    }

    fun suspendListing(listingId: String, reason: String) {
        viewModelScope.launch {
            repository.suspendListing(listingId, reason)
            repository.getListingById(listingId)?.let {
                firebaseManager?.syncListingToFirestore(it)
            }
        }
    }

    fun reportListing(listingId: String, reason: String, details: String) {
        viewModelScope.launch {
            repository.reportListing(listingId, reason, details)
        }
    }

    fun resolveReport(reportId: String, actionTaken: String) {
        viewModelScope.launch {
            repository.resolveReport(reportId, actionTaken)
        }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            repository.dismissReport(reportId)
        }
    }

    fun toggleSave(listingId: String) {
        viewModelScope.launch {
            repository.toggleSave(listingId)
        }
    }

    fun isSaved(listingId: String): Boolean {
        return repository.isSaved(listingId)
    }

    fun updateBusinessProfile(business: BusinessProfile) {
        viewModelScope.launch {
            repository.updateBusinessProfile(business)
        }
    }

    fun updateApiBaseUrl(url: String) {
        viewModelScope.launch {
            repository.updateApiBaseUrl(url)
        }
    }

    // API Keys & Integrations Access
    private val _customGeminiKey = MutableStateFlow(com.example.data.api.GeminiService.customApiKey ?: "")
    val customGeminiKey: StateFlow<String> = _customGeminiKey.asStateFlow()

    fun setCustomGeminiKey(key: String) {
        val trimmed = key.trim()
        com.example.data.api.GeminiService.customApiKey = if (trimmed.isBlank()) null else trimmed
        _customGeminiKey.value = trimmed
    }

    fun validateGeminiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = com.example.data.api.GeminiService.validateApiKey(key)
            result.onSuccess {
                onResult(true, "API Key is valid and verified with Gemini models!")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "API Key verification failed.")
            }
        }
    }

    fun testFirestore(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val res = firebaseManager?.testFirestoreConnection()
            if (res != null) {
                res.onSuccess { msg -> onResult(msg) }
                    .onFailure { err -> onResult("Firestore Error: ${err.localizedMessage}") }
            } else {
                onResult("Firebase connection not initialized yet.")
            }
        }
    }
}
