# Codelligence Free Ads Provider — Implementation Plan

A production-grade, zero-cost business and promotional discovery platform by Codelligence Technologies. Enables startups, local enterprises, creators, service providers, and internship hosts to publish legitimate promotional listings with zero budget while providing community discovery and an in-app moderation workflow.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction and policy decisions confirmed during Phase 1 have been incorporated into this plan:

- **Confirmed Decision 1 (Backend Connectivity)**: Strict remote API client architecture with configurable server endpoint (`BuildConfig` / Settings configurable base URL), along with robust offline-graceful error handling, network interceptors, and local caching for seamless developer testing in the emulator.
- **Confirmed Decision 2 (Moderation & Administration)**: Dedicated in-app Moderator and Administrator portal with multi-role switching (Guest, Registered User, Business Owner, Moderator, Administrator) enabling complete verification and review of listings and reports directly on Android devices.
- **Confirmed Decision 3 (Fair-Use & Validity Policy)**: Unlimited listings per business profile with transparent 60-day active renewal periods, expiration tracking, and spam/duplication detection.

---

## 1. Overview & Core Concept

**Codelligence Free Ads Provider** is a transparent, ₹0-budget discovery and promotion engine built to connect local businesses, tech startups, agencies, course educators, and internship providers with an organic community audience.

- **Primary Persona**:
  - *Entrepreneurs & Creators*: Publish free promotional cards with verified contact links, destination URLs, portfolios, and job/internship vacancies.
  - *Community Users & Job Seekers*: Search, filter by category and city/state, bookmark, share via Android Sharesheet, and report fraudulent or deceptive listings.
  - *Moderators & Admins*: Review queue submissions, investigate user reports, approve/reject with logged reasons, suspend violators, and monitor platform audit logs.
- **Core Value**: Zero-cost promotional reach without deceptive metrics, automated spam, or claims of association with third-party ad networks.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Color Palette (Codelligence Tech Theme)**:
  - Primary: Deep Tech Navy (`#0A1128` background / `#001F54` surface depth)
  - Accent / Vibrant: Electric Blue (`#0066FF`), Neon Cyan (`#00F0FF`), Bright Cobalt (`#1976D2`)
  - Status Indicators: Mint Emerald (`#00C853` for Approved/Active), Amber Alert (`#FFAB00` for Pending Review), Coral Crimson (`#FF3D71` for Rejected/Suspended)
  - Text & Contrast: High-contrast pure white (`#FFFFFF`) display titles and soft silver (`#C5CEE0` / `#8F9BB3`) for body and metadata.
- **Typography & Rhythm**:
  - Crisp, modern sans-serif scale with punchy bold headlines and readable compact descriptions.
  - Touch targets minimum 48dp on all interactive chips, icon buttons, and navigation elements.
- **Layouts & Motion**:
  - Edge-to-edge support with `WindowInsets.safeDrawing` padding.
  - Material 3 Navigation Bar with fluid tabs: Feed, Categories, Submit Ad, Saved, and Profile/Admin.
  - Filter chips with horizontal scroll, animated badge counters, pull-to-refresh feeds, and staggered card reveals.

### Key Screens & User Journeys
1. **Splash & Onboarding**: Codelligence Technologies brand reveal with value propositions (Zero ₹ Fee, Community Trust, Verified Opportunities).
2. **Discovery & Home Feed**: Promoted business cards, internship spotlight, quick category pills, search bar, and city/state location filters.
3. **Category Explorer**: 9 curated sectors (Tech & IT, Software & Web, AI & Data, Education, Internships & Jobs, Local Businesses, Products, Events, Startups).
4. **Listing Details**: Hero header, business badge, 60-day validity counter, verified contact buttons (Call, Email, Website), Android Sharesheet button, Save/Favorite, and Report Abuse sheet.
5. **Free Listing Creator**: Multi-step submission form: Title, description, category, business identity, destination URL, listing type, contact method, validity dates, terms acknowledgement, and instant preview.
6. **Business Profile Management**: Company logo, tagline, category, verified socials, contact coordinates, and active listing count.
7. **My Listings Dashboard**: Status badges (Draft, Pending Review, Approved, Rejected, Suspended, Expired), resubmit/edit actions, and moderator rejection reasons.
8. **In-App Moderation & Admin Portal**:
   - Role switcher for testing (Guest / User / Business / Moderator / Admin).
   - Pending review queue with Approve / Reject modals (reason mandatory).
   - Reported content queue with triage actions (Dismiss, Warn, Suspend listing).
   - Platform audit log & system statistics.
9. **Settings & Compliance**: Privacy policy, community guidelines, ₹0 fee guarantee, account deletion request, and configurable API Base URL.

---

## 3. Key Product Decisions & Trade-Offs

- **Configurable Network Architecture**:
  - *Decision*: Build a Retrofit/OkHttp network layer pointing to a configurable API Base URL (`/api/v1/...`) with an intelligent Mock Server / Fallback Repository.
  - *Why*: Allows immediate live usability in the browser streaming emulator without blocking on an external hosted server, while remaining 100% compliant with the production REST API specification.
- **In-App Multi-Role Administration**:
  - *Decision*: Provide an interactive Admin & Moderator console inside the Android application accessible via profile role switching.
  - *Why*: Evaluators and reviewers can immediately verify the end-to-end moderation lifecycle (submit listing -> switch to Moderator -> approve -> switch back to Guest to see live update).
- **Zero-Budget Integrity**:
  - *Decision*: No mock engagement numbers or synthetic follower counts. Transparent listing dates with 60-day renewal cycle and explicit disclaimer that Codelligence is independent of Google Ads and Meta Ads.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │  Home Feed   │ │ Listing Form │ │  Moderator   │ │ Business Hub │  │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘  │
└─────────┼────────────────┼────────────────┼────────────────┼───────────┘
          ▼                ▼                ▼                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      MVVM ViewModels & StateFlow                       │
│     (HomeViewModel, ListingViewModel, AdminViewModel, AuthViewModel)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       Codelligence Repository Layer                    │
│     (ListingsRepository, AuthRepository, ModerationRepository)         │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    ▼                                ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│       Retrofit / REST Client         │  │     DataStore / Room Cache   │
│   (Configurable API Base URL, JWT)   │  │  (Auth tokens, saved items)  │
└──────────────────────────────────────┘  └──────────────────────────────┘
```

### Core Entities & Domain Models
- **User**: `id`, `name`, `email`, `role` (`GUEST`, `USER`, `BUSINESS_OWNER`, `MODERATOR`, `ADMIN`), `createdAt`.
- **BusinessProfile**: `id`, `userId`, `name`, `logoUrl`, `description`, `category`, `websiteUrl`, `publicEmail`, `publicPhone`, `city`, `state`, `verificationStatus`.
- **Listing**: `id`, `businessId`, `businessName`, `title`, `description`, `category`, `imageUrl`, `destinationUrl`, `listingType` (`BUSINESS`, `PRODUCT`, `SERVICE`, `EVENT`, `COURSE`, `INTERNSHIP`), `location`, `status` (`DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `SUSPENDED`, `EXPIRED`), `rejectionReason`, `validityDays`, `createdAt`, `expiresAt`.
- **Report**: `id`, `listingId`, `reportingUserId`, `reason`, `details`, `status` (`OPEN`, `RESOLVED`, `DISMISSED`), `createdAt`.
- **AuditLogEntry**: `id`, `actorEmail`, `action`, `targetType`, `targetId`, `timestamp`, `details`.

---

## 5. Execution Steps & Verification Plan

1. **Project Dependencies & Manifest**:
   - Check and configure networking (`retrofit`, `converter-moshi`, `okhttp`, `logging-interceptor`), Coroutines, Material 3 icons extended, and Coil.
   - Sync Android app name in `strings.xml` and `metadata.json` ("Codelligence Free Ads Provider").
2. **Domain Models & Network Client**:
   - Define all models, DTOs, API interface (`CodelligenceApiService`), and repository contracts.
   - Implement repository with live remote calls and local resilience engine.
3. **UI Theme & Component Toolkit**:
   - Codelligence Tech Navy theme in `Theme.kt`, `Color.kt`, and `Type.kt`.
   - Shared cards: ListingCard, CategoryChip, StatusBadge, RoleSwitcherBanner.
4. **Feature Screens**:
   - Splash & Onboarding.
   - Home Feed with search and filters.
   - Category Browsing.
   - Listing Details with Sharesheet and Report dialog.
   - Free Listing Submission with live preview.
   - My Listings & Business Profile manager.
   - In-App Moderation Queue & Admin Audit Dashboard.
   - Saved Listings, Notifications, Settings, and Help & Support.
5. **Backend & Admin Web Artifacts**:
   - Provide full backend REST API definitions, schema migrations, seed data, and Docker Compose configuration for the complete stack.
6. **Compilation & Validation**:
   - Run `compile_applet` to ensure pristine compilation and error-free execution.
