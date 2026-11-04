# Bildirim aksiyonları ve pil ayarları kanalı yansımayla çağrılmaz; yine de MainActivity korunur.
-keep class com.ilactakip.ilac_takip.MainActivity { *; }
# flutter_local_notifications zamanlanmış bildirimleri GSON ile saklar (v19+ kurallar GSON'dan gelir).
-keepattributes Signature
-keepattributes *Annotation*
