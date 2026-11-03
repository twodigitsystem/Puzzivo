# Puzzivo Enterprise ProGuard/R8 Configuration

# ================================
# Keep Game Model Classes & State
# ================================
-keep class com.tds.puzzivo.core.model.** { *; }
-keep class com.tds.puzzivo.core.storage.UserPreferences { *; }
-keep class com.tds.puzzivo.feature.**State { *; }

# ================================
# Room Database
# ================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ================================
# Hilt / Dagger
# ================================
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn dagger.hilt.**

# ================================
# Kotlinx Serialization
# ================================
-keepattributes *Annotation*, InnerClasses, Signature
-keep,allowobfuscation @kotlinx.serialization.Serializable class *
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# ================================
# Crashlytics & Stack Traces
# ================================
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepattributes *Annotation*

# ================================
# Firebase
# ================================
-dontwarn com.google.firebase.**

# ================================
# Kotlin Coroutines
# ================================
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ================================
# Remove Debug Logging in Release
# ================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# ================================
# Optimize Enums
# ================================
-optimizations !class/unboxing/enum
