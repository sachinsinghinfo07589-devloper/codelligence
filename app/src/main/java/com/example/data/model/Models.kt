package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class UserRole {
    GUEST,
    USER,
    BUSINESS_OWNER,
    MODERATOR,
    ADMIN
}

enum class ListingType(val label: String) {
    BUSINESS("Business"),
    PRODUCT("Product"),
    SERVICE("Service"),
    EVENT("Event"),
    COURSE("Course"),
    INTERNSHIP("Internship")
}

enum class ListingStatus(val label: String) {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended"),
    EXPIRED("Expired")
}

@JsonClass(generateAdapter = true)
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole = UserRole.USER,
    val businessId: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class BusinessProfile(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val logoUrl: String? = null,
    val shortDescription: String,
    val category: String,
    val websiteUrl: String? = null,
    val publicEmail: String? = null,
    val publicPhone: String? = null,
    val city: String? = null,
    val state: String? = null,
    val linkedinUrl: String? = null,
    val twitterUrl: String? = null,
    val isVerified: Boolean = false,
    val verificationNote: String = "Community Verified via Codelligence Terms",
    val activeListingCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Listing(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val businessName: String,
    val title: String,
    val description: String,
    val category: String,
    val listingType: ListingType,
    val imageUrl: String? = null,
    val destinationUrl: String? = null,
    val city: String? = null,
    val state: String? = null,
    val isOnlineAvailable: Boolean = true,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val status: ListingStatus = ListingStatus.PENDING_REVIEW,
    val rejectionReason: String? = null,
    val validityDays: Int = 60, // 60-day renewal policy as confirmed
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000),
    val viewCount: Int = 0,
    val clickCount: Int = 0,
    val reportCount: Int = 0,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList()
)

data class CategoryItem(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val listingCount: Int
)

@JsonClass(generateAdapter = true)
data class Report(
    val id: String = UUID.randomUUID().toString(),
    val listingId: String,
    val listingTitle: String,
    val businessName: String,
    val reporterEmail: String,
    val reason: String,
    val details: String,
    val status: String = "OPEN", // OPEN, RESOLVED, DISMISSED
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val actorEmail: String,
    val actorRole: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "INFO", // INFO, APPROVAL, REJECTION, RENEWAL
    val isRead: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)

@JsonClass(generateAdapter = true)
data class ApiListResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val total: Int = 0,
    val page: Int = 1
)

@JsonClass(generateAdapter = true)
data class ApiItemResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)
