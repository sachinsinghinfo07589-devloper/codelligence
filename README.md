# Codelligence Free Ads Provider

A complete, production-oriented Android application and full-stack platform owned and branded by **Codelligence Technologies**.

The platform enables startups, small enterprises, freelance developers, technical creators, educational institutes, and internship providers to publish promotional listings for free (₹0 fee) and discover legitimate businesses organically.

---

## 1. Zero-Budget (₹0) Architecture & Policy

- **Transparent Listing Price**: ₹0 initial and ongoing listing fee. No hidden fees or forced recurring payments.
- **Fair-Use Limits**:
  - Unlimited active listings per business subject to human moderation.
  - 60-day promotional validity period.
  - Free, instant 60-day renewal cycle to prevent dead links.
- **Zero Third-Party Claims**: Not affiliated with Google Ads, Meta Ads, LinkedIn Ads, or any paid ad networks.
- **Zero Synthetic Metrics**: No fake view generators, artificial bots, or deceptive engagement metrics.

---

## 2. Technology Stack

- **Android App**: Kotlin 2.2, Jetpack Compose, Material 3, MVVM Architecture, Retrofit 2, Moshi, Coil, Coroutines & StateFlow.
- **Backend**: Node.js, Express, TypeScript, Zod validation, JWT authentication, rate limiting, and security headers.
- **Database & ORM**: PostgreSQL, Prisma ORM with migrations and seed data.
- **Admin Dashboard**: React, TypeScript, Vite, Tailwind CSS.
- **DevOps**: Docker & Docker Compose.

---

## 3. Directory Structure

```
codelligence-free-ads/
├── app/                  # Android Jetpack Compose Application
│   ├── src/main/java/com/example/
│   │   ├── data/         # Models, Retrofit API interface, CodelligenceRepository
│   │   ├── ui/
│   │   │   ├── components/ # TopBar, ListingCard, Badges, Modals, RoleSwitcher
│   │   │   ├── screens/    # Home, Categories, Submit, Detail, Admin, Hub, Settings
│   │   │   ├── theme/      # Dark Navy & Cyan/Electric Blue Color System
│   │   │   └── viewmodel/  # MainViewModel
│   │   └── MainActivity.kt
│   └── build.gradle.kts
├── backend/              # Node.js Express REST API
│   ├── prisma/schema.prisma
│   ├── src/server.ts
│   └── package.json
├── admin-web/            # Companion React + Tailwind Admin Dashboard
│   ├── src/App.tsx
│   └── package.json
├── docker-compose.yml    # Local multi-container development setup
├── metadata.json         # Platform metadata
└── README.md
```

---

## 4. In-App Multi-Role Experience

For reviewers and evaluators testing on the Android Streaming Emulator, an interactive **Role Switcher** is embedded in the top bar:
- **Guest**: Browse public verified promotions.
- **User**: Search, bookmark favorites, report abuse.
- **Business Owner**: Manage business profile, post free listings, renew listings.
- **Moderator**: Review pending queue, approve submissions, reject with required reasons.
- **Admin**: Full moderation queue, abuse reports triage, audit trail logs, and system metrics.

---

## 5. Local Development via Docker

```bash
# Start PostgreSQL, Backend REST API, and Admin Web
docker compose up -d

# Backend runs at http://localhost:4000
# Admin Web runs at http://localhost:3000
```
