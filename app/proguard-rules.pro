# =============================================================================
# TrackOne — R8 / ProGuard rules
# =============================================================================
# R8 is enabled for release builds (isMinifyEnabled = true). Everything below is
# required because the corresponding library resolves types by reflection, which
# R8 cannot see. Removing a rule here will typically surface as a crash only in
# a release build, so please test `assembleRelease` after editing.
# =============================================================================

# Keep line numbers so release crash reports stay readable, but hide the
# original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures / annotations are needed by reflective JSON mappers.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*,RuntimeVisibleAnnotations,AnnotationDefault

# -----------------------------------------------------------------------------
# Kotlin
# -----------------------------------------------------------------------------
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# -----------------------------------------------------------------------------
# Room — entities are instantiated reflectively by generated code
# -----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# App entities (defensive — they are also @Entity annotated)
-keep class com.dev.trackone.**.data.local.** { *; }

# -----------------------------------------------------------------------------
# Hilt / Dagger
# -----------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclasseswithmembers class * { @javax.inject.Inject <init>(...); }
-dontwarn dagger.hilt.**

# -----------------------------------------------------------------------------
# AndroidX Biometric / DataStore / Compose
# -----------------------------------------------------------------------------
-keep class androidx.biometric.** { *; }
-dontwarn androidx.datastore.**

# Compose keeps what it needs via its own consumer rules; only @Preview helpers
# would be stripped, which is desirable in release.

# -----------------------------------------------------------------------------
# App models — kept so the on-device JSON snapshot survives obfuscation
# -----------------------------------------------------------------------------
-keep class com.dev.trackone.**.domain.model.** { *; }
-keep class com.dev.trackone.backup.** { *; }

# Enum values are looked up by name in the snapshot format (ExpenseCategory,
# PaymentMode) — do not let R8 rename or remove them.
-keepclassmembers enum com.dev.trackone.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}
