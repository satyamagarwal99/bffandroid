# Keep metadata and annotations used by Retrofit, Gson, and Kotlin-reflective libraries.
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Retrofit service interfaces use method and parameter annotations at runtime.
-keep interface com.gobff.getfriends.data.ApiService { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson deserializes API DTOs via reflection. Field names are protected by @SerializedName,
# but keeping model classes avoids constructor/field stripping surprises in release builds.
-keep class com.gobff.getfriends.data.model.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn com.google.gson.**

# Firebase Messaging service is launched by the framework from the manifest.
-keep class com.gobff.getfriends.service.BffFirebaseMessagingService { *; }
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.**

# Agora RTC SDK uses native/reflection entry points internally.
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Juspay HyperSDK is loaded dynamically when available.
-dontwarn in.juspay.**
