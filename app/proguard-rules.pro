# Rhetorica release keep rules.

-keep class com.rhetorica.app.data.local.** { *; }
-keep class com.rhetorica.app.data.seed.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.appwidget.AppWidgetProvider

-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,includedescriptorclasses class com.rhetorica.app.**$$serializer { *; }
-keepclassmembers class com.rhetorica.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.rhetorica.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.rhetorica.app.BuildConfig { *; }
