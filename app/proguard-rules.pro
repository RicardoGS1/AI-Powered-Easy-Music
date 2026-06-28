# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation interface com.virtualworld.easymusic.data.remote.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn sun.misc.**

-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <init>(...);
}
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# WorkManager (transitivo vía play-services-ads): R8 full mode elimina constructores usados por reflexión.
-keep class androidx.work.** { <init>(...); }
-dontwarn org.jaudiotagger.**
-keep class org.jaudiotagger.** { *; }
-keep class androidx.work.WorkerParameters