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

# Dogfooding fixtures for the ngaburake obfuscation-verify plugin itself (this app applies its
# own plugin and configures these two classes as sensitivePackages — see
# obfuscationVerify {} in this module's build.gradle.kts).
#
# PaymentManager is trivial enough (one constructor, one method, one call site) that R8
# inlines/merges it away entirely without a keep rule, so it would never appear in mapping.txt
# at all. allowobfuscation keeps the class as a distinct, checkable entity while still letting
# R8 rename it — this is also the realistic rule shape a security team would use for a
# sensitive class that must survive as its own type but should still be obfuscated.
-keep,allowobfuscation class com.rezacah.ngaburake.PaymentManager

# Same allowobfuscation treatment as PaymentManager above — keeps ApiKeyStore checkable as its
# own type instead of being inlined away, while still letting R8 rename it.
#
# An earlier, deliberately overly broad `-keep class com.rezacah.ngaburake.ApiKeyStore { *; }`
# rule was used here to manually validate that verifyObfuscation catches this exact violation;
# it has been removed after that validation so this build stays green.
-keep,allowobfuscation class com.rezacah.ngaburake.ApiKeyStore