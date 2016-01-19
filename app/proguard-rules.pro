# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/julianlo/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ProGuard will complain because of MultiDexing, then fail to build a signed APK
# Here we (unfortunately) liberally hide warnings for all packages affected by MultiDexing.
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.**
-dontwarn okio.**
-dontwarn org.joda.time.**