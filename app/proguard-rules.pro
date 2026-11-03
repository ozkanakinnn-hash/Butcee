# Ramazan Rehberi - R8 kuralları

# Moshi (codegen + reflection)
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn org.jetbrains.annotations.**

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Firebase Messaging servisi
-keep class com.example.service.MyFirebaseMessagingService { *; }

# Veri modelleri (JSON ile kaydediliyor)
-keep class com.example.data.** { *; }
