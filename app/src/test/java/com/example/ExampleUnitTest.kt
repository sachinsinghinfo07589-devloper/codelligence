package com.example

import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.model.UserRole
import com.example.data.repository.CodelligenceRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: CodelligenceRepository

    @Before
    fun setUp() {
        repository = CodelligenceRepository()
    }

    @Test
    fun listingSubmission_entersPendingReviewWith60DayValidity() {
        val listing = repository.submitListing(
            title = "Cloud DevOps Consulting",
            description = "High-availability Kubernetes and Terraform configurations for startups.",
            category = "Technology and IT services",
            listingType = ListingType.SERVICE,
            destinationUrl = "https://example.com/devops",
            city = "Bengaluru",
            state = "Karnataka",
            isOnlineAvailable = true,
            contactEmail = "devops@example.com",
            contactPhone = "+91 98765 00000"
        )

        assertEquals(ListingStatus.PENDING_REVIEW, listing.status)
        assertEquals(60, listing.validityDays)
        assertTrue(listing.expiresAt > listing.createdAt)
    }

    @Test
    fun moderationApprove_publishesListing() {
        val listing = repository.submitListing(
            title = "Test Course",
            description = "Sample course description for testing approval workflow.",
            category = "Education and training",
            listingType = ListingType.COURSE,
            destinationUrl = null,
            city = null,
            state = null,
            isOnlineAvailable = true,
            contactEmail = null,
            contactPhone = null
        )

        repository.approveListing(listing.id)
        val approved = repository.getListingById(listing.id)

        assertNotNull(approved)
        assertEquals(ListingStatus.APPROVED, approved?.status)
        assertNull(approved?.rejectionReason)
    }

    @Test
    fun moderationReject_setsStatusAndReason() {
        val listing = repository.submitListing(
            title = "Deceptive Internship Offer",
            description = "Unverified offering requiring upfront training fee deposit.",
            category = "Internships and jobs",
            listingType = ListingType.INTERNSHIP,
            destinationUrl = null,
            city = null,
            state = null,
            isOnlineAvailable = true,
            contactEmail = null,
            contactPhone = null
        )

        val reason = "Unlawful hidden fee detected in violation of community terms"
        repository.rejectListing(listing.id, reason)
        val rejected = repository.getListingById(listing.id)

        assertNotNull(rejected)
        assertEquals(ListingStatus.REJECTED, rejected?.status)
        assertEquals(reason, rejected?.rejectionReason)
    }

    @Test
    fun renewal_extends60DayPeriod() {
        val listing = repository.submitListing(
            title = "Renewable Service",
            description = "Service listing testing the 60-day renewal cycle at ₹0.",
            category = "Services",
            listingType = ListingType.SERVICE,
            destinationUrl = null,
            city = null,
            state = null,
            isOnlineAvailable = true,
            contactEmail = null,
            contactPhone = null
        )

        repository.renewListing(listing.id)
        val renewed = repository.getListingById(listing.id)

        assertEquals(ListingStatus.APPROVED, renewed?.status)
        assertTrue(renewed!!.expiresAt > System.currentTimeMillis() + (58L * 24 * 60 * 60 * 1000))
    }

    @Test
    fun roleSwitching_updatesRoleAndAudits() {
        repository.setUserRole(UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, repository.currentUser.value.role)

        val latestAudit = repository.auditLogs.value.first()
        assertEquals("ROLE_SWITCH", latestAudit.action)
    }
}
