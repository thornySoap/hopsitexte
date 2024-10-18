# https://github.com/JetBrains/compose-multiplatform/issues/4883#issuecomment-2156012785
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.ui.text.platform.ReflectionUtil { *; }

# We're excluding Material 2 from the project as we're using Material 3
-dontwarn androidx.compose.material.**

# Kotlinx coroutines rules seems to be outdated with the latest version of Kotlin and Proguard
-keep class kotlinx.coroutines.** { *; }

# FilePicker on Windows
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
