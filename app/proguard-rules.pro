# ===================================================
# General
# ===================================================

# Hide original source file names in stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Keep generic signatures (needed for Kotlin generics / reflection)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ===================================================
# Kotlin
# ===================================================

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }
-dontwarn kotlin.**

# ===================================================
# Kotlinx Serialization
# ===================================================

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep all your network model data classes (used for JSON parsing)
-keep class com.qcp.aioverlay.data.model.** { *; }

# ===================================================
# Hilt / Dagger
# ===================================================

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <fields>;
    @dagger.hilt.* <methods>;
}
-dontwarn dagger.**
-dontwarn hilt_aggregated_deps.**

# ===================================================
# Room
# ===================================================

-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# ===================================================
# Coroutines
# ===================================================

-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ===================================================
# Compose
# ===================================================

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===================================================
# Google Generative AI SDK
# ===================================================

-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ===================================================
# AndroidX / Lifecycle / Navigation
# ===================================================

-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.datastore.** { *; }
-dontwarn androidx.**

# ===================================================
# Your app — keep entry points, strip internals
# ===================================================

# Keep Application class
-keep class com.qcp.aioverlay.AIOverlayApp { *; }

# Keep Accessibility Service (must be kept — Android binds it by name)
-keep class com.qcp.aioverlay.service.AIAccessibilityService { *; }

# Keep domain models used across layers
-keep class com.qcp.aioverlay.domain.model.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
