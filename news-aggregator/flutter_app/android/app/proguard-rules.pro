# Haber Akışı — R8 (release küçültme) koruma kuralları
#
# WorkManager (AdMob ve Firebase'in arka plan işleri) Room veritabanını yansıma ile yükler:
# WorkDatabase_Impl silinirse uygulama açılışta "Failed to create an instance of
# androidx.work.impl.WorkDatabase" ile çöker (emülatör kontrolüyle doğrulandı).
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# flutter_local_notifications: kayıtlı bildirimleri Gson ile okur (genel tip bilgisi korunmalı)
-keep class com.dexterous.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
