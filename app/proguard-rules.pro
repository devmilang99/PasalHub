# Aggressive Obfuscation
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Keep Line Numbers for crash reporting (optional, remove for max security)
#-keepattributes SourceFile,LineNumberTable

# Supabase & Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Java Management (required by Ktor debug detector)
-keep class java.lang.management.** { *; }

# Hilt/Dagger
-keep class dagger.hilt.** { *; }
-keep class com.psl.pasalhub.core.di.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class com.psl.pasalhub.core.database.data.** { *; }

# Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# RootBeer
-keep class com.scottyab.rootbeer.** { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }

#Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**