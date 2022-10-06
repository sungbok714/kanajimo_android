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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#noinspection ShrinkerUnresolvedReference
-keep public class org.jsoup.** {
	public *;
}

#----Glide--- start
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  #noinspection ShrinkerUnresolvedReference
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------

# OkHttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-dontwarn android.support.v4.**,org.slf4j.**,com.google.android.gms.**

-dontwarn com.fasterxml.jackson.databind.**

# TedPermission
-keepattributes *Annotation*
-keepclassmembers class ** {
    #noinspection ShrinkerUnresolvedReference
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keep class com.messeesang.kanajimo.** { *; }

# Kakao
-keep class com.kakao.** { *; }
-keepattributes Signature
-keepclassmembers class * {
  public static <fields>;
  public *;
}
-dontwarn android.support.v4.**,org.slf4j.**,com.google.android.gms.**
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter

# Naver Login
-keep public class com.nhn.android.naverlogin.** {
    public protected *;
}

# Appsflyer Start
-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; }
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
  public *;
}
# Appsflyer End