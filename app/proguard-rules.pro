# ═══════════════════════════════════════════════════════════════════════
# ProGuard / R8 rules для Journal of Dream
# ═══════════════════════════════════════════════════════════════════════

# ── Общие ─────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# ── Firebase ──────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Firestore — КРИТИЧНО: модели данных для сериализации
-keep class com.dreamjournal.journalofdream.model.Dream { *; }
-keep class com.dreamjournal.journalofdream.model.Location { *; }
-keep class com.dreamjournal.journalofdream.model.Category { *; }
-keep class com.dreamjournal.journalofdream.model.DreamLocationCrossRef { *; }
-keep class com.dreamjournal.journalofdream.model.Technique { *; }
-keep class com.dreamjournal.journalofdream.model.TechniqueComment { *; }

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ── Room Database ─────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Jetpack Compose ───────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ── Google Sign-In ────────────────────────────────────────────────────
-keep class com.google.android.gms.auth.api.signin.** { *; }
-dontwarn com.google.android.gms.auth.api.signin.**

# ── Kotlin ────────────────────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ── AndroidX ──────────────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-dontwarn androidx.**

# ── Предотвращение удаления enum ──────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
