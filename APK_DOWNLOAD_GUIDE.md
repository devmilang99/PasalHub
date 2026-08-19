# 🛒 PasalHub APK Download & Installation Guide

> [!IMPORTANT]
> This document has been consolidated into the main [README.md](README.md#installation). Please
> refer to the README for the most up-to-date installation instructions and build artifacts.

## 🎯 Quick Start

*
    *
*⬇️ [Download Latest Debug APK](https://github.com/devmilang99/PasalHub/releases/tag/latest-debug)
**

This document provides complete instructions for downloading and installing the latest PasalHub
APK build.

---

## 📦 What You're Downloading

You are downloading **PasalHub** - a next-generation native Android marketplace built for speed,
intelligence, and seamless shopping.

- **Frontend**: Kotlin & Jetpack Compose (100%)
- **AI Engine**: Google Gemini API & TensorFlow Lite
- **Backend**: Supabase (Postgres, Realtime, Auth)
- **Architecture**: Clean Architecture & MVVM

This APK is automatically built and packaged from the latest source code via GitHub Actions CI/CD
pipeline.

---

## ✅ System Requirements

Before installation, ensure your device meets these requirements:

| Requirement                     | Specification                              |
|---------------------------------|--------------------------------------------|
| **Minimum Android Version**     | Android 7.0 (API Level 24)                 |
| **Recommended Android Version** | Android 10.0 or higher                     |
| **Storage Space**               | At least 150MB free                        |
| **RAM**                         | Minimum 3GB (4GB+ recommended)             |
| **Network**                     | Internet connection required for AI & Sync |

---

## 🚀 Installation Methods

### Method 1: Direct Installation (Easiest)

1. **Download the APK**
    - Click the download link above to get the `app-debug.apk` file.
    - Wait for download to complete.

2. **Prepare Your Device**
    - Go to: **Settings → Security**
    - Find and toggle on **"Unknown Sources"** or **"Install from Unknown Sources"**.
    - (This allows installation from outside Google Play Store).

3. **Install the Application**
    - Open your file manager.
    - Navigate to Downloads folder.
    - Tap on the `app-debug.apk` file.
    - Confirm installation when prompted.
    - Wait for installation to complete.

4. **Launch PasalHub**
    - Find the app in your app drawer.
    - Tap to open for the first time.
    - Grant all requested permissions for the best experience:
        - ✓ Internet access
        - ✓ Camera (for visual search)
        - ✓ Location (for local deals)
        - ✓ Notifications (for order updates)

### Method 2: Command Line Installation (ADB)

For developers with Android SDK tools installed:

```bash
# Step 1: Connect your Android device via USB
# Enable USB Debugging on device: Settings → Developer Options → USB Debugging
adb devices

# Step 2: Install the APK
adb install path/to/app-debug.apk

# Step 3: Launch the app
adb shell am start -n com.psl.pasalhub/.core.application.MainActivity
```

---

## 🔒 Security & Permissions

PasalHub is built with modern security standards. Here's what the app accesses and why:

| Permission               | Purpose                                                |
|--------------------------|--------------------------------------------------------|
| **INTERNET**             | Connect to Supabase servers, Gemini AI, and sync data  |
| **CAMERA**               | On-device visual product search via TFLite             |
| **ACCESS_FINE_LOCATION** | Personalized local marketplace recommendations         |
| **POST_NOTIFICATIONS**   | Real-time order tracking and live updates              |
| **READ_MEDIA_IMAGES**    | Profile photo selection and image-based product search |

**All permissions are:**

- ✓ Explicitly requested at runtime.
- ✓ Can be individually denied (with feature limitations).
- ✓ Revocable at any time via Settings.

---

## 🐛 Troubleshooting

### Installation Issues

| Problem                                            | Solution                                                 |
|----------------------------------------------------|----------------------------------------------------------|
| **"Installation blocked" message**                 | Enable "Unknown Sources" in Settings → Security          |
| **"Insufficient storage space"**                   | Free up at least 200MB and retry                         |
| **"Installation failed" or APK corrupted**         | Re-download the APK file (may be incomplete)             |
| **"App not installed as package appears invalid"** | Ensure you are installing the correct architecture build |

### Runtime Issues

| Problem                        | Solution                                                                 |
|--------------------------------|--------------------------------------------------------------------------|
| **App crashes on startup**     | Clear app cache: Settings → Apps → PasalHub → Storage → Clear Cache      |
| **AI features not responding** | Ensure active internet connection and valid Gemini API key is configured |
| **Visual search not working**  | Grant Camera permissions in App Settings                                 |

---

## 🔄 Updating

When new builds are released:

1. Download the new APK from the GitHub Releases page.
2. Install it over the existing version.
3. Your data and settings will be preserved by Android.
4. App will restart and show new features.

---

## 📊 Build Information

```
Build Type: Debug (CI Generated)
Namespace: com.psl.pasalhub
Target SDK: 37 (Android 15)
Min SDK: 24 (Android 7.0)
Architecture: arm64-v8a, x86_64
Primary Tech: Kotlin, Compose, Room, Hilt, Supabase, TFLite
```

---

## 📞 Support & Feedback

### Report Issues

- Found a bug? [Open an issue on GitHub](https://github.com/devmilang99/PasalHub/issues)
- Include: Device model, Android version, and error message.

### Get Help

- Check the repository README for developer documentation.
- Review app logs for debugging if you are a developer.

---

**Last Updated:** August 19, 2026  
**Repository:** [devmilang99/PasalHub](https://github.com/devmilang99/PasalHub)  
**License:** Apache 2.0
