<div align="center">

<img src="https://img.shields.io/badge/Kotlin-2.4%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge" />
<img src="https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge" />

<br /><br />

<!-- Replace with your actual logo -->
# 🛒 PasalHub

**A next-generation native Android marketplace built for speed, intelligence, and seamless shopping.**
Discover, search, and shop — powered by on-device AI and real-time sync.

[Features](#-features) · [Getting Started](#-getting-started) · [Architecture](#-architecture) · [Screenshots](#-screenshots) · [Contributing](#-contributing)

---

</div>

## 📖 Overview

**PasalHub** is a production-grade native Android e-commerce application. It combines modern mobile architecture with agentic AI workflows and on-device computer vision to deliver a fast, intelligent, and privacy-conscious mobile shopping experience.

Built with scalability and performance at its core, PasalHub follows strict Clean Architecture principles, leverages on-device machine learning for visual product search, and integrates conversational AI to help users find exactly what they're looking for — all while staying offline-first.

---

## ✨ Features

### 🤖 AI & Intelligence
- **Gemini AI Conversational Assistant** — Integrated via the Google Gemini API & Function Calling to transform natural language queries into dynamic catalog filtering and personalized recommendations.
- **On-Device Visual Search** *(Search by Image)* — Embedded TensorFlow Lite (MobileNetV3 Small Quantized) model for real-time, privacy-focused image embeddings and feature extraction directly on the client.
- **Smart Recommendations** — Context-aware product suggestions driven by user behavior and conversational intent.

### 📦 Catalog & Orders
- **Real-Time Catalog Sync** — Synchronized with Supabase Realtime (Postgres DB) for instant inventory updates.
- **Multi-Vendor Listings** — Support for multiple sellers (e.g., "Pasal Hub Official", "Luxury Direct") within a unified catalog experience.
- **Live Order Tracking** — Real-time order status updates from checkout to delivery via WorkManager-backed tracking.
- **Dynamic Inventory Management** — Stock levels reflected instantly across all connected clients.

### 🔐 Security & Auth
- **Google ID & Supabase Auth** — Frictionless, secure onboarding with federated identity and Credential Manager support.
- **Secure Local Storage** — Preferences and session data managed via Android `SharedPreferences` with modular auth state handling.
- **Session Management** — Secure token handling and automatic session refresh via Supabase Auth integration.

### ⚡ Performance & UX
- **Offline-First Architecture** — Full browsing and cart functionality without an internet connection via Room Database with RemoteMediator for seamless paging.
- **Sub-60fps Rendering** — Smooth UI across form factors, validated with Macrobenchmark and optimized Compose states.
- **Fast Cold Starts** — Baseline Profiles for optimized launch times.
- **Adaptive Layouts** — Responsive Compose UI across phones, foldables, and tablets using Material 3 Adaptive Layouts.
- **Agentic System Actions** — Exposes catalog discovery via Android AppFunctions for system-level shortcuts and AI agent interactions.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language & UI** | Kotlin (2.4.0), Jetpack Compose (Material 3), Adaptive Layouts |
| **Architecture** | Clean Architecture, MVVM, Coroutines, Kotlin Flow |
| **AI / ML** | Google Gemini API (Function Calling), TensorFlow Lite (MobileNetV3), CameraX |
| **Backend & Sync** | Supabase (Postgres, Realtime, Auth), Ktor Client |
| **Local Data** | Room Database (with Paging 3), SharedPreferences, WorkManager |
| **DI & Testing** | Hilt, Roborazzi (Screenshot Testing), Mockito, JUnit 4, Espresso |
| **Performance** | Baseline Profiles, Macrobenchmark, R8 / ProGuard Optimization |
| **CI/CD** | Gradle Version Catalogs, Secrets Gradle Plugin |

---

## 📐 Architecture

PasalHub follows a **Clean Architecture** pattern with a layered separation of concerns and a **uni-directional data flow (UDF)**. The codebase is organized by feature modules:

```
app/src/main/java/com/psl/pasalhub/
├── ai/                      # Gemini AI integration, AppFunctions, and search routing
├── auth/                    # Login, Register, Forgot Password flows
├── core/                    # Cross-cutting concerns
│   ├── application/         # MainActivity, App class, Global ViewModels
│   ├── auth/                # Supabase Auth repository implementations
│   ├── database/            # Room DB, Entities, and DAOs
│   ├── di/                  # Hilt modules
│   ├── networking/          # Ktor client, DTOs, and Interceptors
│   └── sync/                # DataSyncRepository and SyncManager
├── dashboard/               # Core marketplace features
│   ├── cart/                # Cart management and Order Review
│   ├── home/                # Product listing and filtering
│   ├── order/               # Order history and Live Tracking
│   ├── products/            # Product Detail and RemoteMediators
│   └── profile/             # User profile and settings
├── initial/                 # App startup (Splash, Onboarding, Theme)
├── ui/                      # Global theme and design system
└── visualsearch/            # TFLite Visual Search Engine
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- [Android Studio](https://developer.android.com/studio) (latest stable or preview for SDK 37 support)
- JDK 17+
- Android SDK (minSdk 24+)
- A [Supabase](https://supabase.com) project (Postgres + Auth + Realtime enabled)
- A [Google Gemini API key](https://ai.google.dev/)

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/your-username/PasalHub.git
cd PasalHub
```

**2. Configure Secrets**

Create a `.env` file in the root directory (referencing `.env.example`):

```properties
SUPABASE_URL="your_supabase_url"
SUPABASE_ANON_KEY="your_supabase_anon_key"
GEMINI_API_KEY="your_gemini_api_key"
```

The `Secrets Gradle Plugin` will automatically expose these to the `BuildConfig` during build.

**3. Sync & build**

```bash
./gradlew assembleDebug
```

*Note: The `generateLocalKeystore` task will automatically run to set up your debug signing environment.*

**4. Run the app**

Open the project in Android Studio and run it on an emulator/device (API 24+), or via CLI:

```bash
./gradlew installDebug
```

---

## 🧪 Running Tests

```bash
# Unit tests (JUnit 4, Mockito)
./gradlew test

# Instrumented tests
./gradlew connectedCheck

# Screenshot tests (Roborazzi)
./gradlew verifyRoborazziDebug

# Macrobenchmark (startup & scroll performance)
./gradlew :baselineprofile:connectedCheck
```

---

## 📱 Screenshots

> _Coming soon — screenshots will be added upon first stable release._

| Home & Catalog | Visual Search | AI Assistant | Order Tracking |
|:---:|:---:|:---:|:---:|
| _(soon)_ | _(soon)_ | _(soon)_ | _(soon)_ |

---

## 🗺 Roadmap

- [x] Core catalog & order management
- [x] On-device visual search (MobileNetV3)
- [x] Gemini conversational assistant with Function Calling
- [x] Baseline Profiles & Macrobenchmark integration
- [x] Android AppFunctions for system-level discovery
- [ ] Multi-vendor seller dashboard
- [ ] Wishlist & price-drop notifications
- [ ] AR product preview
- [ ] Wear OS companion app

---

## 🤝 Contributing

We welcome contributions! Please follow the [Conventional Commits](https://www.conventionalcommits.org/) convention for pull requests.

---

## 📄 License

```
Copyright 2025 Pasal Hub

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<div align="center">

Built with ❤️ using Kotlin & Jetpack Compose

</div>
