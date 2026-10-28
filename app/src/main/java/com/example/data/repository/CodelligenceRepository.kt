package com.example.data.repository

import com.example.data.api.CodelligenceApiService
import com.example.data.api.NetworkClient
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CodelligenceRepository(
    private var apiService: CodelligenceApiService = NetworkClient.createService()
) {

    // Current Session State
    private val _currentUser = MutableStateFlow<User>(
        User(
            id = "usr_codelligence_01",
            name = "Aman Sharma",
            email = "aman@techspark.io",
            role = UserRole.BUSINESS_OWNER,
            businessId = "biz_01"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _currentBusiness = MutableStateFlow<BusinessProfile?>(
        BusinessProfile(
            id = "biz_01",
            userId = "usr_codelligence_01",
            name = "TechSpark Software Solutions",
            shortDescription = "Cloud migration, Android app development, and open-source tooling for modern startups.",
            category = "Software and web development",
            websiteUrl = "https://techspark.example.com",
            publicEmail = "contact@techspark.example.com",
            publicPhone = "+91 98765 43210",
            city = "Bengaluru",
            state = "Karnataka",
            isVerified = true,
            activeListingCount = 2
        )
    )
    val currentBusiness: StateFlow<BusinessProfile?> = _currentBusiness.asStateFlow()

    // In-memory persistent database store
    private val _listings = MutableStateFlow<List<Listing>>(getInitialSeedListings())
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    private val _savedListingIds = MutableStateFlow<Set<String>>(setOf("listing_01", "listing_03"))
    val savedListingIds: StateFlow<Set<String>> = _savedListingIds.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(getInitialSeedReports())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(getInitialSeedAuditLogs())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(getInitialSeedNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val categories = listOf(
        CategoryItem("cat_1", "Technology and IT services", "Managed infrastructure, DevOps, cybersecurity, IT consulting", "Computer", 18),
        CategoryItem("cat_2", "Software and web development", "Custom mobile apps, web design, APIs, full-stack systems", "Code", 24),
        CategoryItem("cat_3", "AI and data science", "LLM solutions, computer vision, data analytics, ML pipelines", "Psychology", 14),
        CategoryItem("cat_4", "Education and training", "Coding bootcamps, technical certifications, skill workshops", "School", 19),
        CategoryItem("cat_5", "Internships and jobs", "Verified tech internships, engineering fellowships, junior roles", "Work", 31),
        CategoryItem("cat_6", "Local businesses", "Professional repair, local consulting, artisan stores, printing", "Storefront", 22),
        CategoryItem("cat_7", "Products and services", "SaaS tools, productivity widgets, enterprise subscriptions", "ShoppingBag", 15),
        CategoryItem("cat_8", "Events and communities", "Developer meetups, hackathons, founder circles, tech webinars", "Event", 12),
        CategoryItem("cat_9", "Startups and entrepreneurship", "Early-stage ventures, accelerator pitches, co-founder search", "RocketLaunch", 16)
    )

    // Role Switching for interactive review
    fun setUserRole(role: UserRole) {
        val user = _currentUser.value
        _currentUser.value = user.copy(role = role)
        logAudit("ROLE_SWITCH", "User switched active role to ${role.name}")
    }

    fun updateUser(name: String, email: String) {
        val user = _currentUser.value
        _currentUser.value = user.copy(name = name, email = email)
    }

    fun updateBusinessProfile(business: BusinessProfile) {
        _currentBusiness.value = business
        logAudit("BUSINESS_UPDATE", "Business profile updated: ${business.name}")
    }

    fun mergeCloudListings(cloudListings: List<Listing>) {
        if (cloudListings.isEmpty()) return
        val current = _listings.value
        val map = current.associateBy { it.id }.toMutableMap()
        for (item in cloudListings) {
            map[item.id] = item
        }
        _listings.value = map.values.sortedByDescending { it.createdAt }
    }

    // Listings Operations
    fun getApprovedListings(): List<Listing> {
        val now = System.currentTimeMillis()
        return _listings.value.filter { it.status == ListingStatus.APPROVED && it.expiresAt > now }
    }

    fun getPendingListings(): List<Listing> {
        return _listings.value.filter { it.status == ListingStatus.PENDING_REVIEW }
    }

    fun getMyListings(): List<Listing> {
        val bizId = _currentBusiness.value?.id ?: "biz_01"
        return _listings.value.filter { it.businessId == bizId }
    }

    fun getListingById(id: String): Listing? {
        return _listings.value.find { it.id == id }
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
        val biz = _currentBusiness.value ?: BusinessProfile(
            userId = _currentUser.value.id,
            name = _currentUser.value.name + "'s Enterprise",
            shortDescription = "Verified Member on Codelligence",
            category = category
        )

        val newListing = Listing(
            id = "lst_" + UUID.randomUUID().toString().take(8),
            businessId = biz.id,
            businessName = biz.name,
            title = title,
            description = description,
            category = category,
            listingType = listingType,
            destinationUrl = destinationUrl,
            city = city,
            state = state,
            isOnlineAvailable = isOnlineAvailable,
            contactEmail = contactEmail ?: biz.publicEmail,
            contactPhone = contactPhone ?: biz.publicPhone,
            status = ListingStatus.PENDING_REVIEW, // Enters moderation review queue
            validityDays = 60,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000)
        )

        _listings.value = listOf(newListing) + _listings.value

        // Notify user
        addNotification(
            title = "Listing Submitted for Review",
            message = "Your listing \"$title\" has been submitted for free community review. Verification typically takes < 24 hours.",
            type = "INFO"
        )

        logAudit("LISTING_SUBMISSION", "Submitted listing \"$title\" by ${biz.name}")
        return newListing
    }

    fun renewListing(listingId: String) {
        val updated = _listings.value.map { listing ->
            if (listing.id == listingId) {
                listing.copy(
                    expiresAt = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000),
                    status = ListingStatus.APPROVED
                )
            } else listing
        }
        _listings.value = updated
        addNotification("Listing Renewed", "Your 60-day promotional period has been renewed successfully at ₹0.", "RENEWAL")
        logAudit("LISTING_RENEWAL", "Renewed 60-day listing ID $listingId")
    }

    // Moderation Actions
    fun approveListing(listingId: String) {
        val updated = _listings.value.map { listing ->
            if (listing.id == listingId) {
                listing.copy(
                    status = ListingStatus.APPROVED,
                    rejectionReason = null
                )
            } else listing
        }
        _listings.value = updated
        val item = _listings.value.find { it.id == listingId }
        addNotification(
            title = "Promotion Approved!",
            message = "Your promotional listing \"${item?.title}\" was verified and published to the live public feed.",
            type = "APPROVAL"
        )
        logAudit("MODERATION_APPROVE", "Approved listing: ${item?.title} (ID: $listingId)")
    }

    fun rejectListing(listingId: String, reason: String) {
        val updated = _listings.value.map { listing ->
            if (listing.id == listingId) {
                listing.copy(
                    status = ListingStatus.REJECTED,
                    rejectionReason = reason
                )
            } else listing
        }
        _listings.value = updated
        val item = _listings.value.find { it.id == listingId }
        addNotification(
            title = "Listing Needs Corrections",
            message = "Your listing \"${item?.title}\" was rejected: $reason. Please update your listing to resubmit.",
            type = "REJECTION"
        )
        logAudit("MODERATION_REJECT", "Rejected listing ID $listingId with reason: $reason")
    }

    fun suspendListing(listingId: String, reason: String) {
        val updated = _listings.value.map { listing ->
            if (listing.id == listingId) {
                listing.copy(
                    status = ListingStatus.SUSPENDED,
                    rejectionReason = reason
                )
            } else listing
        }
        _listings.value = updated
        logAudit("MODERATION_SUSPEND", "Suspended listing ID $listingId: $reason")
    }

    // User reporting
    fun reportListing(listingId: String, reason: String, details: String) {
        val listing = getListingById(listingId)
        val report = Report(
            listingId = listingId,
            listingTitle = listing?.title ?: "Unknown Listing",
            businessName = listing?.businessName ?: "Unknown Business",
            reporterEmail = _currentUser.value.email,
            reason = reason,
            details = details
        )
        _reports.value = listOf(report) + _reports.value
        logAudit("REPORT_FILED", "Report filed against listing $listingId: $reason")
    }

    fun resolveReport(reportId: String, actionTaken: String) {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(status = "RESOLVED") else it
        }
        logAudit("REPORT_RESOLVED", "Report $reportId resolved: $actionTaken")
    }

    fun dismissReport(reportId: String) {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(status = "DISMISSED") else it
        }
        logAudit("REPORT_DISMISSED", "Report $reportId dismissed as benign")
    }

    // Favorites & Bookmarks
    fun toggleSave(listingId: String) {
        val set = _savedListingIds.value.toMutableSet()
        if (set.contains(listingId)) {
            set.remove(listingId)
        } else {
            set.add(listingId)
        }
        _savedListingIds.value = set
    }

    fun isSaved(listingId: String): Boolean {
        return _savedListingIds.value.contains(listingId)
    }

    fun updateApiBaseUrl(url: String) {
        NetworkClient.currentBaseUrl = url
        apiService = NetworkClient.createService(url)
        logAudit("CONFIG_UPDATE", "Updated remote API base URL to $url")
    }

    private fun addNotification(title: String, message: String, type: String) {
        val item = NotificationItem(
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    private fun logAudit(action: String, details: String) {
        val entry = AuditLogEntry(
            actorEmail = _currentUser.value.email,
            actorRole = _currentUser.value.role.name,
            action = action,
            targetType = "PLATFORM",
            targetId = "SYS",
            details = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    private fun getInitialSeedListings(): List<Listing> {
        val dayMs = 24L * 60 * 60 * 1000
        val now = System.currentTimeMillis()

        return listOf(
            Listing(
                id = "listing_01",
                businessId = "biz_01",
                businessName = "TechSpark Software Solutions",
                title = "Scalable Microservices & Android Engineering",
                description = "We build high-concurrency cloud backends and responsive Kotlin Android apps for high-growth tech ventures. Specializing in Docker, Node.js, and Jetpack Compose.",
                category = "Software and web development",
                listingType = ListingType.SERVICE,
                destinationUrl = "https://techspark.example.com/services",
                city = "Bengaluru",
                state = "Karnataka",
                isOnlineAvailable = true,
                contactEmail = "contact@techspark.example.com",
                contactPhone = "+91 98765 43210",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (5 * dayMs),
                expiresAt = now + (55 * dayMs),
                viewCount = 184,
                clickCount = 42,
                isFeatured = true,
                tags = listOf("Android", "Kotlin", "Cloud", "API")
            ),
            Listing(
                id = "listing_02",
                businessId = "biz_02",
                businessName = "Codelligence Technologies Academy",
                title = "Full-Stack Software Engineering Fellowship (Winter 2026)",
                description = "12-week intensive hands-on fellowship covering Kotlin, Jetpack Compose, TypeScript, PostgreSQL, and scalable microservices. Mentorship by senior engineers.",
                category = "Internships and jobs",
                listingType = ListingType.INTERNSHIP,
                destinationUrl = "https://codelligence.tech/fellowship",
                city = "Hyderabad",
                state = "Telangana",
                isOnlineAvailable = true,
                contactEmail = "fellowships@codelligence.tech",
                contactPhone = "+91 91234 56789",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (2 * dayMs),
                expiresAt = now + (58 * dayMs),
                viewCount = 390,
                clickCount = 112,
                isFeatured = true,
                tags = listOf("Internship", "Kotlin", "FullStack", "Stipend")
            ),
            Listing(
                id = "listing_03",
                businessId = "biz_03",
                businessName = "Aura Data Labs",
                title = "Enterprise LLM Fine-Tuning & Vector Search Integration",
                description = "Transform proprietary business manuals and documents into hallucination-resistant internal intelligence bots using LangChain, pgvector, and FastAPI.",
                category = "AI and data science",
                listingType = ListingType.SERVICE,
                destinationUrl = "https://auradatalabs.example.com",
                city = "Pune",
                state = "Maharashtra",
                isOnlineAvailable = true,
                contactEmail = "ai@auradatalabs.example.com",
                contactPhone = "+91 98220 11223",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (8 * dayMs),
                expiresAt = now + (52 * dayMs),
                viewCount = 145,
                clickCount = 28,
                tags = listOf("AI", "LLM", "DataScience", "Search")
            ),
            Listing(
                id = "listing_04",
                businessId = "biz_04",
                businessName = "Nexus Cloud Security",
                title = "Zero-Trust Cloud Infrastructure Audit for FinTech",
                description = "Comprehensive pen-testing, AWS/GCP IAM configuration review, and compliance readiness (SOC2, ISO 27001). Free initial vulnerability scope assessment.",
                category = "Technology and IT services",
                listingType = ListingType.SERVICE,
                destinationUrl = "https://nexuscloudsec.example.com",
                city = "Gurugram",
                state = "Haryana",
                isOnlineAvailable = true,
                contactEmail = "security@nexuscloudsec.example.com",
                contactPhone = "+91 99100 88776",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (12 * dayMs),
                expiresAt = now + (48 * dayMs),
                viewCount = 92,
                clickCount = 19,
                tags = listOf("Security", "Cloud", "Audit", "ZeroTrust")
            ),
            Listing(
                id = "listing_05",
                businessId = "biz_05",
                businessName = "Founders Desk Co-working",
                title = "Flexible Hot Desks & Creator Studios in Koramangala",
                description = "High-speed gigabit Wi-Fi, ergonomic Herman Miller seating, podcast recording room, and weekend demo nights. Free 1-day pass for seed startups.",
                category = "Local businesses",
                listingType = ListingType.BUSINESS,
                destinationUrl = "https://foundersdesk.example.com",
                city = "Bengaluru",
                state = "Karnataka",
                isOnlineAvailable = false,
                contactEmail = "desk@foundersdesk.example.com",
                contactPhone = "+91 98800 55443",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (15 * dayMs),
                expiresAt = now + (45 * dayMs),
                viewCount = 210,
                clickCount = 65,
                tags = listOf("Coworking", "Office", "Community", "Startups")
            ),
            Listing(
                id = "listing_06",
                businessId = "biz_06",
                businessName = "IndieFlow Dev Studio",
                title = "Open-Source Offline Database Sync Library for Android",
                description = "Lightweight KMP library providing conflict-free replicated data types (CRDT) for Android and desktop apps. MIT Licensed, 100% free forever.",
                category = "Products and services",
                listingType = ListingType.PRODUCT,
                destinationUrl = "https://github.com/indieflow/sync-engine",
                city = "Chennai",
                state = "Tamil Nadu",
                isOnlineAvailable = true,
                contactEmail = "dev@indieflow.example.com",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (18 * dayMs),
                expiresAt = now + (42 * dayMs),
                viewCount = 340,
                clickCount = 88,
                tags = listOf("OpenSource", "Android", "Database", "Library")
            ),
            Listing(
                id = "listing_07",
                businessId = "biz_07",
                businessName = "NextGen Innovators Meet",
                title = "AI in Production: Bengaluru Founders & Engineers Hackathon",
                description = "48-hour in-person hackathon with ₹1,00,000 in open API credits, expert architectural critiques, and angel investor showcase. Registration is ₹0.",
                category = "Events and communities",
                listingType = ListingType.EVENT,
                destinationUrl = "https://innovatorsmeet.example.com/hackathon",
                city = "Bengaluru",
                state = "Karnataka",
                isOnlineAvailable = false,
                contactEmail = "events@innovatorsmeet.example.com",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (3 * dayMs),
                expiresAt = now + (57 * dayMs),
                viewCount = 480,
                clickCount = 140,
                isFeatured = true,
                tags = listOf("Hackathon", "AI", "Founders", "FreeEvent")
            ),
            Listing(
                id = "listing_08",
                businessId = "biz_08",
                businessName = "VentureScale Studio",
                title = "Technical Co-Founder Search: AI Logistics SaaS",
                description = "Bootstrapped B2B route-optimization platform seeking experienced Kotlin/Python engineer with distributed systems background. Equity-first partnership.",
                category = "Startups and entrepreneurship",
                listingType = ListingType.BUSINESS,
                destinationUrl = "https://venturescale.example.com",
                city = "Mumbai",
                state = "Maharashtra",
                isOnlineAvailable = true,
                contactEmail = "founders@venturescale.example.com",
                status = ListingStatus.APPROVED,
                validityDays = 60,
                createdAt = now - (10 * dayMs),
                expiresAt = now + (50 * dayMs),
                viewCount = 195,
                clickCount = 44,
                tags = listOf("CoFounder", "Equity", "Logistics", "Startup")
            ),
            // Pending Listings for Moderation Demo
            Listing(
                id = "listing_09",
                businessId = "biz_09",
                businessName = "QuickFix Web Masters",
                title = "Professional WordPress & Shopify Store Speed Optimization",
                description = "Improve Google Core Web Vitals to 95+ score. Clean database bloat, minify assets, and configure Cloudflare caching. 100% money-back guarantee.",
                category = "Software and web development",
                listingType = ListingType.SERVICE,
                destinationUrl = "https://quickfixweb.example.com",
                city = "Hyderabad",
                state = "Telangana",
                isOnlineAvailable = true,
                contactEmail = "support@quickfixweb.example.com",
                status = ListingStatus.PENDING_REVIEW,
                validityDays = 60,
                createdAt = now - (1 * dayMs),
                expiresAt = now + (59 * dayMs),
                tags = listOf("WebSpeed", "Shopify", "WordPress")
            ),
            Listing(
                id = "listing_10",
                businessId = "biz_10",
                businessName = "Alpha Cloud Tech",
                title = "Python Data Engineering Summer Internship",
                description = "3-month paid internship working on real ETL pipelines with Apache Airflow and Snowflake. Candidates should know SQL and pandas fundamentals.",
                category = "Internships and jobs",
                listingType = ListingType.INTERNSHIP,
                destinationUrl = "https://alphacloudtech.example.com/careers",
                city = "Noida",
                state = "Uttar Pradesh",
                isOnlineAvailable = true,
                contactEmail = "careers@alphacloudtech.example.com",
                status = ListingStatus.PENDING_REVIEW,
                validityDays = 60,
                createdAt = now,
                expiresAt = now + (60 * dayMs),
                tags = listOf("Internship", "Python", "Data", "Remote")
            )
        )
    }

    private fun getInitialSeedReports(): List<Report> {
        return listOf(
            Report(
                id = "rep_01",
                listingId = "listing_05",
                listingTitle = "Flexible Hot Desks in Koramangala",
                businessName = "Founders Desk Co-working",
                reporterEmail = "user_review@domain.com",
                reason = "Outdated contact number",
                details = "Called the provided number and it went to an unrelated residence. Please check with business owner.",
                status = "OPEN"
            )
        )
    }

    private fun getInitialSeedAuditLogs(): List<AuditLogEntry> {
        val now = System.currentTimeMillis()
        val hourMs = 60 * 60 * 1000L
        return listOf(
            AuditLogEntry(
                actorEmail = "admin@codelligence.tech",
                actorRole = "ADMIN",
                action = "SYSTEM_INITIALIZE",
                targetType = "SERVER",
                targetId = "INIT",
                timestamp = now - (24 * hourMs),
                details = "Codelligence Free Ads Provider system initialized with ₹0 fair-use policies."
            ),
            AuditLogEntry(
                actorEmail = "moderator@codelligence.tech",
                actorRole = "MODERATOR",
                action = "MODERATION_APPROVE",
                targetType = "LISTING",
                targetId = "listing_02",
                timestamp = now - (6 * hourMs),
                details = "Approved listing: Full-Stack Fellowship (verified legitimate company credentials)."
            ),
            AuditLogEntry(
                actorEmail = "moderator@codelligence.tech",
                actorRole = "MODERATOR",
                action = "MODERATION_APPROVE",
                targetType = "LISTING",
                targetId = "listing_07",
                timestamp = now - (2 * hourMs),
                details = "Approved event: Bengaluru Founders Hackathon."
            )
        )
    }

    private fun getInitialSeedNotifications(): List<NotificationItem> {
        val now = System.currentTimeMillis()
        val hourMs = 60 * 60 * 1000L
        return listOf(
            NotificationItem(
                id = "notif_01",
                title = "Welcome to Codelligence Free Ads",
                message = "Publish unlimited legitimate business profiles, products, services, and internships for free with ₹0 listing fees.",
                timestamp = now - (12 * hourMs),
                type = "INFO"
            ),
            NotificationItem(
                id = "notif_02",
                title = "Listing Active: TechSpark Solutions",
                message = "Your promotional listing was approved and has 55 days remaining in its initial 60-day cycle.",
                timestamp = now - (4 * hourMs),
                type = "APPROVAL"
            )
        )
    }
}
