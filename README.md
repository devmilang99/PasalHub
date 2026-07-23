<div align="center">
🛒 PasalHub
Next-Gen Android E-Commerce Platform

A high-performance native Android marketplace blending modern mobile architecture with agentic AI workflows and on-device computer vision.

Show Image Show Image Show Image Show Image

</div>
📖 Overview

PasalHub is a native Android marketplace built with Kotlin and Jetpack Compose, designed to deliver a fast, intelligent, and seamless mobile shopping experience. It combines real-time backend sync, on-device machine learning, and conversational AI to reimagine how users discover and shop for products — all while staying offline-first and privacy-conscious.

✨ Key Features
	Feature	Description
🤖	Gemini AI Conversational Assistant	Integrated via the Google Gemini API & Function Calling to transform natural language queries into dynamic catalog filtering and personalized recommendations.
📷	On-Device Visual Search (Search by Image)	Embedded TensorFlow Lite (MobileNetV3) model for real-time, privacy-focused image embeddings and feature extraction directly on the client.
📦	Real-Time Catalog & Order Management	Synchronized with Supabase Realtime (Postgres DB) for instant order tracking, dynamic inventory updates, and multi-vendor product listings.
⚡	Offline-First & High Performance	Built on Room Database, Ktor Client, and Paging 3 with Baseline Profiles for rapid launch times and smooth sub-60fps UI rendering across form factors.
🔐	Secure Authentication	Integrated Google ID & Supabase Auth for frictionless, secure onboarding.
🛠️ Tech Stack & Architecture

PasalHub follows strict Clean Architecture principles with the MVVM pattern and a uni-directional data flow (UDF).

app/
 ├── data/          # Repositories, Supabase DTOs, Room DB, TFLite Inference
 ├── domain/        # Use Cases, Domain Models, Repository Contracts
 └── presentation/  # Jetpack Compose Screens, ViewModels, UI State
Domain Breakdown
Domain	Technologies & Libraries
Language & UI	Kotlin, Jetpack Compose (Material 3), Adaptive Layouts
Architecture	Clean Architecture, MVVM, Coroutines, Kotlin Flow
AI / ML	Google Gemini API (Function Calling), TensorFlow Lite (MobileNetV3), CameraX
Backend & Sync	Supabase (Postgres, Realtime, Auth), Ktor Client
Local Data	Room Database, EncryptedSharedPreferences, Paging 3
DI & Testing	Hilt / Koin, Roborazzi (Screenshot Testing), MockK, JUnit 5
Performance	Baseline Profiles, Macrobenchmark, R8 / ProGuard Optimization
⚡ System Actions & Extensions
Android AppFunctions: Exposes key in-app workflows — order tracking, catalog search — to system-level agents, enabling voice-activated and agentic interactions outside the core app UI.
🚀 Getting Started
Prerequisites
Android Studio (latest stable)
JDK 17+
A Supabase project (Postgres + Auth + Realtime enabled)
A Google Gemini API key
Setup
Clone the repository
bash
   git clone https://github.com/<your-username>/pasalhub.git
   cd pasalhub
Configure local properties Add the following to local.properties (or your preferred secrets mechanism):
properties
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   GEMINI_API_KEY=your_gemini_api_key
Build & Run
bash
   ./gradlew assembleDebug

Or open the project in Android Studio and run it on an emulator/device (API 24+).

🧪 Testing
bash
./gradlew test                # Unit tests (JUnit 5, MockK)
./gradlew connectedCheck      # Instrumented tests
./gradlew verifyRoborazziDebug # Screenshot tests
📸 Screenshots

Add app screenshots or a demo GIF here to showcase the UI and key flows.

🗺️ Roadmap
 Multi-vendor seller dashboard
 Wishlist & price-drop notifications
 Expanded visual search categories
 Wear OS companion app
🤝 Contributing

Contributions are welcome! Please open an issue to discuss major changes before submitting a pull request.

Fork the repo
Create your feature branch (git checkout -b feature/amazing-feature)
Commit your changes (git commit -m 'Add some amazing feature')
Push to the branch (git push origin feature/amazing-feature)
Open a Pull Request
📄 License

This project is licensed under the MIT License — see the LICENSE file for details.

<div align="center">

Built with ❤️ using Kotlin & Jetpack Compose

</div>
