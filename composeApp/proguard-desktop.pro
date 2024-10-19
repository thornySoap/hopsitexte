# https://github.com/JetBrains/compose-multiplatform/issues/4883#issuecomment-2156012785
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }

-dontwarn androidx.compose.material.**

# Kotlinx coroutines
-keep class kotlinx.coroutines.** { *; }

# FilePicker on Windows
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
