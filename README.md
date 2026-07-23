<div align="center">

<img src="https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
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
- **Gemini AI Conversational Assistant** — Integrated via the Google Gemini API & Function Calling to transform natural language queries into dynamic catalog filtering and personalized recommendations
- **On-Device Visual Search** *(Search by Image)* — Embedded TensorFlow Lite (MobileNetV3) model for real-time, privacy-focused image embeddings and feature extraction directly on the client
- **Smart Recommendations** — Context-aware product suggestions driven by user behavior and conversational intent

### 📦 Catalog & Orders
- **Real-Time Catalog Sync** — Synchronized with Supabase Realtime (Postgres DB) for instant inventory updates
- **Multi-Vendor Listings** — Support for multiple sellers within a unified catalog experience
- **Live Order Tracking** — Real-time order status updates from checkout to delivery
- **Dynamic Inventory Management** — Stock levels reflected instantly across all connected clients

### 🔐 Security & Auth
- **Google ID & Supabase Auth** — Frictionless, secure onboarding with federated identity
- **Encrypted Local Storage** — Sensitive data protected via `EncryptedSharedPreferences`
- **Session Management** — Secure token handling and automatic session refresh

### ⚡ Performance & UX
- **Offline-First Architecture** — Full browsing and cart functionality without an internet connection via Room Database
- **Sub-60fps Rendering** — Smooth UI across form factors, validated with Macrobenchmark
- **Fast Cold Starts** — Baseline Profiles for optimized launch times
- **Adaptive Layouts** — Responsive Compose UI across phones, foldables, and tablets
- **Agentic System Actions** — Exposes order tracking and catalog search via Android AppFunctions for voice-activated, system-level interactions

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language & UI** | Kotlin, Jetpack Compose (Material 3), Adaptive Layouts |
| **Architecture** | Clean Architecture, MVVM, Coroutines, Kotlin Flow |
| **AI / ML** | Google Gemini API (Function Calling), TensorFlow Lite (MobileNetV3), CameraX |
| **Backend & Sync** | Supabase (Postgres, Realtime, Auth), Ktor Client |
| **Local Data** | Room Database, EncryptedSharedPreferences, Paging 3 |
| **DI & Testing** | Hilt / Koin, Roborazzi (Screenshot Testing), MockK, JUnit 5 |
| **Performance** | Baseline Profiles, Macrobenchmark, R8 / ProGuard Optimization |
| **CI/CD** | GitHub Actions, Gradle Version Catalogs |

---

## 📐 Architecture

PasalHub follows a **Clean Architecture** pattern with a layered separation of concerns and a **uni-directional data flow (UDF)**:

```
app/
├── data/
│   ├── remote/             # Supabase DTOs, Ktor API services
│   ├── local/               # Room DB, DAOs, EncryptedSharedPreferences
│   ├── ml/                  # TFLite inference, MobileNetV3 embeddings
│   └── repository/          # Repository implementations
│
├── domain/
│   ├── model/                # Domain models
│   ├── repository/          # Abstract repository contracts
│   └── usecase/              # Single-responsibility use cases
│
├── presentation/
│   ├── catalog/               # Product listing, filtering, search
│   ├── visualsearch/        # Camera capture & image search flow
│   ├── assistant/            # Gemini conversational AI screens
│   ├── orders/                # Order tracking & history
│   ├── cart/                   # Cart & checkout
│   └── auth/                   # Login, onboarding
│
├── di/                          # Hilt / Koin modules
└── core/
    ├── navigation/           # Navigation graph, routes
    ├── theme/                  # Compose theming
    └── util/                     # Formatters, extensions, constants
```

Each feature module follows the same internal structure:

```
feature/
├── data/          # Data sources & repository implementation
├── domain/        # Use cases & domain contracts
└── presentation/  # Composable screens, ViewModels, UI state
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- [Android Studio](https://developer.android.com/studio) (latest stable)
- JDK 17+
- Android SDK (minSdk 24+)
- A [Supabase](https://supabase.com) project (Postgres + Auth + Realtime enabled)
- A [Google Gemini API key](https://ai.google.dev/)

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/<your-username>/pasalhub.git
cd pasalhub
```

**2. Configure local properties**

Add the following to `local.properties` (or your preferred secrets mechanism):

```properties
SUPABASE_URL=your_supabase_url
SUPABASE_ANON_KEY=your_supabase_anon_key
GEMINI_API_KEY=your_gemini_api_key
```

**3. Sync & build**

```bash
./gradlew assembleDebug
```

**4. Run the app**

Open the project in Android Studio and run it on an emulator/device (API 24+), or via CLI:

```bash
./gradlew installDebug
```

---

## 🧪 Running Tests

```bash
# Unit tests (JUnit 5, MockK)
./gradlew test

# Instrumented tests
./gradlew connectedCheck

# Screenshot tests
./gradlew verifyRoborazziDebug

# Macrobenchmark (startup & scroll performance)
./gradlew :benchmark:connectedCheck
```

---

## 🔐 Security & Data Handling

PasalHub is built with the following practices in mind:

- All network traffic uses **TLS**
- Sensitive local data protected via **EncryptedSharedPreferences**
- Visual search inference runs **entirely on-device** — images never leave the client for embedding generation
- Authentication delegated to **Google ID & Supabase Auth**, avoiding local credential storage
- Regular dependency audits via Gradle's dependency verification

> ⚠️ **Responsible Disclosure:** Found a security vulnerability? Please open a private security advisory (or contact the maintainer directly) rather than opening a public issue.

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
- [x] Gemini conversational assistant
- [x] Baseline Profiles & Macrobenchmark integration
- [ ] Multi-vendor seller dashboard
- [ ] Wishlist & price-drop notifications
- [ ] Expanded visual search categories
- [ ] AR product preview
- [ ] Wear OS companion app

---

## 🤝 Contributing

We welcome contributions from the community! Please read our contributing guidelines before opening a pull request.

**Development workflow:**

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature-name`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to your branch: `git push origin feat/your-feature-name`
5. Open a Pull Request against `develop`

**Commit message convention** (following [Conventional Commits](https://www.conventionalcommits.org/)):

```
feat:     New feature
fix:      Bug fix
docs:     Documentation changes
style:    Formatting, no logic change
refactor: Code restructuring
test:     Adding or updating tests
chore:    Build process, dependencies
```

Please ensure your code passes all tests and lint checks (`./gradlew lint`) before submitting.

---

## 📄 License

```
MIT License

Copyright (c) 2025 <Your Name>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📬 Contact & Support

| Channel | Link |
|---|---|
| 📧 General | [devmilang99@gmail.com](mailto:your-email@example.com) |
| 🐛 Issues | [GitHub Issues](https://github.com/<your-username>/pasalhub/issues) |

---

<div align="center">

Built with ❤️ using Kotlin & Jetpack Compose

</div>
