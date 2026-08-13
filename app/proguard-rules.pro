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


# Keep data binding classes
-keep class androidx.databinding.** { *; }

# Keep the generated BR class, which is used for referencing resources in layouts
-keepclassmembers class **.databinding.** {
    <fields>;
}
# Keep classes required for Navigation component with Data Binding
-keep class * implements androidx.navigation.NavArgs
-keep class * implements androidx.navigation.dynamicfeatures.fragment.ui.AbstractProgressFragment



-keep class androidx.appcompat.widget.** { *; }
-dontwarn com.festival.flyer.postermaker.activities.**
-dontwarn com.festival.flyer.postermaker.adapter.**
-dontwarn com.festival.flyer.postermaker.adManager.**
-dontwarn com.festival.flyer.postermaker.fragment.**
-dontwarn com.festival.flyer.postermaker.view.**