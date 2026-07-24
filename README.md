# Pasal Hub 🛍️

**Pasal Hub** is a premium, modern Android e-commerce application built with a focus on high-end
UI/UX and cutting-edge technology. It features AI-powered product discovery, visual search
capabilities, and a fully reactive architecture.

---

## 🚀 Key Features

- **✨ AI-Powered Discovery**: Intelligent search and recommendations powered by Google AI (Gemini)
  to help users find exactly what they need.
- **📷 Visual Search**: Integrated TensorFlow Lite engine for identifying products through the
  camera.
- **📱 Adaptive UI**: Fully responsive design using Jetpack Compose and Material 3 Adaptive Layouts,
  optimized for phones and tablets.
- **🔄 Real-time Sync**: Seamless synchronization of cart and profile data across devices using
  Supabase Realtime.
- **🌙 Dynamic Theming**: Sophisticated Dark and Light mode support with Material 3 color schemes.
- **📦 Offline First**: Robust local persistence with Room database and background synchronization
  via WorkManager.
- **🔐 Secure Auth**: Multiple authentication flows including Email/Password and Google Sign-In via
  Credential Manager.

---

## 📸 Screenshots

| Onboarding Flow | Home Dashboard (Dark) | AI Search Interface |
| :---: | :---: | :---: |
| ![Onboarding](https://via.placeholder.com/300x600?text=Onboarding+Screen) | ![Home Dark](https://via.placeholder.com/300x600?text=Home+Dashboard+Dark) | ![AI Search](https://via.placeholder.com/300x600?text=AI+Search+Animation) |

| P                               roduct Details |                                Order Review | User Profile |
| ::-----------------------------------------------------------------------------::---: | :---: |
| ! [Product Details](https://via.placeholder.com/300x600?text=Product+Details) | ![Order Review](https://via.placeholder.com/300x600?text=Order+Review) | ![Profile](https://via.placeholder.com/300x600?text=User+Profile) |

> [!TIP]
> **Action Required**: Replace these placeholders with actual screenshots from your `assets/` or
`metadata/` folder for a professional look.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100%
  Declarative)
- **Dependency Injection
  **: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Backend-as-a-Service**: [Supabase](https://supabase.com/) (Auth, Database, Realtime)
- **Networking**: [Ktor](https://ktor.io/)
- **AI/ML
  **: [Google AI SDK (Gemini)](https://ai.google.dev/), [TensorFlow Lite](https://www.tensorflow.org/lite)
- **Persistence**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Concurrency**: Kotlin Coroutines & Flow
- **Testing**: Roborazzi (Screenshot testing), Mockito, Espresso

---

## 🏗️ Architecture

The project follows **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)**
pattern:

- **Core**: Shared utilities, networking, and base components.
- **Features**: Modularized feature packages (Auth, Dashboard, AI Search, etc.) each containing its
  own UI, Domain, and Data layers.
- **Usecases**: Decoupled business logic for complex operations like order placement and AI
  filtering.

---

## ⚙️ Setup & Installation

### Prerequisites

- Android Studio Jellyfish or newer.
- JDK 17+.
- A Supabase project (for API keys).
- A Google Cloud project (for Gemini API and Google Auth).

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/PasalHub.git
   ```
2. **Environment Variables**:
   Create a `.env` file in the root directory (based on `.env.example`) and add your keys:
   ```env
   SUPABASE_URL="your_supabase_url"
   SUPABASE_ANON_KEY="your_anon_key"
   GEMINI_API_KEY="your_gemini_key"
   ```
3. **Build**:
   Open the project in Android Studio and sync Gradle. The `generateLocalKeystore` task will
   automatically run to set up your debug environment.
4. **Run**:
   Deploy to an emulator or physical device.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Created with ❤️ for the Android Portfolio.*
