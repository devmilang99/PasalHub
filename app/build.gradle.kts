import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.plugin)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.baselineprofile.plugin)
    kotlin("plugin.serialization") version "2.4.0"
}

tasks.register("generateLocalKeystore") {
    description = ""
    val keystoreFile = file("${rootDir}/local-debug.keystore")
    outputs.file(keystoreFile)

    doLast {
        if (!keystoreFile.exists()) {
            println("Generating local debug keystore...")
            val command = listOf(
                "keytool", "-genkey", "-v",
                "-keystore", keystoreFile.absolutePath,
                "-alias", "androiddebugkey",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "10000",
                "-keypass", "android",
                "-storepass", "android",
                "-dname", "CN=Android Debug,O=Android,C=US"
            )
            val process = ProcessBuilder(command).inheritIO().start()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw GradleException("Failed to generate local debug keystore. Exit code: $exitCode")
            }
        }
    }
}

// Ensure the keystore is generated before any build task
tasks.matching { it.name.startsWith("preBuild") }.configureEach {
    dependsOn("generateLocalKeystore")
}

val dbVersion = 1

val signingProps = file("${rootDir}/signing.properties").takeIf { it.exists() }?.let {
    Properties().apply { load(it.inputStream()) }
}

android {
    namespace = "com.psl.pasalhub"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.psl.pasalhub"
        minSdk = 24
        targetSdk = 37
        versionCode = dbVersion
        versionName = "1.0.$dbVersion"

        buildConfigField("int", "DATABASE_VERSION", dbVersion.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootDir}/local-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            val keystorePath =
                signingProps?.getProperty("RELEASE_KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
                ?: "my-upload-key.jks"
            storeFile = rootProject.file(keystorePath)
            storePassword = signingProps?.getProperty("RELEASE_STORE_PASSWORD")
                ?: System.getenv("STORE_PASSWORD")
            keyAlias = signingProps?.getProperty("RELEASE_KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
                    ?: "upload"
            keyPassword =
                signingProps?.getProperty("RELEASE_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            // Disable PNG crunching in debug builds
            isCrunchPngs = false

            // Disables resource shrinking/obfuscation on debug runs to save time
            isMinifyEnabled = false

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        mlModelBinding = true
    }
    sourceSets {
        getByName("main") {
            assets.srcDirs("../assets")
        }
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}


// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.googleid)
    implementation(libs.errorprone.annotations)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.play.services.location)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite)
    testImplementation(libs.androidx.core)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.appfunctions.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.androidx.compiler)
    ksp(libs.androidx.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("appfunctions:aggregateAppFunctions", "true")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.ktor") {
            useVersion(libs.versions.ktor.get())
        }
    }
}
