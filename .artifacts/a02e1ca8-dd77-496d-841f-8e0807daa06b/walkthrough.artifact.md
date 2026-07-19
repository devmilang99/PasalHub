# Walkthrough: Android Studio Performance Optimization

I have implemented several optimizations to reduce Android Studio's background overhead and improve build efficiency for the **Pasal Hub** project.

## Changes Made

### 🚀 Gradle Build Speed
- **File System Watching**: Added `org.gradle.vfs.watch=true` to `gradle.properties`. This allows Gradle to keep track of file changes in real-time, significantly reducing the time spent scanning the disk during builds and syncs.

### 🧹 Reduced IDE Overhead
- **Streamlined Inspection Profile**: Created a new [Optimized Project Default](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/.idea/inspectionProfiles/Optimized_Project_Default.xml) profile.
    - **Disabled Spellchecker**: Disabling real-time spellchecking reduces CPU usage during typing, especially in large files.
    - **Disabled Inconsistent Line Separators**: Prevents unnecessary background checks on file formatting.
    - **Disabled TODO Indexing**: Reduces the indexing load if you don't rely heavily on the IDE's TODO tool window.
- **Profile Activation**: Configured [profiles_settings.xml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/.idea/inspectionProfiles/profiles_settings.xml) to automatically use this optimized profile.

### 📁 Indexing Optimization
- **Asset Exclusion**: Modified [.idea/misc.xml](file:///D:/For Portfolio/Android Porfolio Projects/PasalHub/.idea/misc.xml) to exclude the `assets/` directory from indexing. This prevents the IDE from attempting to parse and index large binary files like `mobile_clip.tflite`, which can cause the IDE to hang or use excessive memory.

## Verification Results
- **Gradle Sync**: Successfully completed.
- **Configuration Check**: `gradlew help` confirms that the new properties are being picked up by the build system.

> [!TIP]
> If you need to re-enable Spellchecking or other inspections, you can switch back to the "Project Default" profile in **Settings > Editor > Inspections**.
