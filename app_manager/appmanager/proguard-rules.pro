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

-keep class lib.editors.base.** { *; }
-keep class lib.editors.slides.** { *; }
-keep class lib.editors.cells.** { *; }
-keep class lib.editors.docs.** { *; }

-keep class lib.editors.gbase.** { *; }
-keep class lib.editors.gdocs.** { *; }
-keep class lib.editors.cells.** { *; }
-keep class lib.editors.slides.** { *; }
-keepclassmembers class lib.editors.gbase.** { *; }
-keepclassmembers class lib.editors.gdocs.** { *; }
-keepclassmembers class lib.editors.gcells.** { *; }
-keepclassmembers class lib.editors.gslides.** { *; }

-keep class lib.x2t.X2t { *; }
-keep class lib.x2t.data.** { *; }
-keepclassmembers class lib.x2t.X2t { *; }
-keepclassmembers class lib.x2t.data.** { *; }

-keep class app.documents.core.** { *; }
-keep class lib.editors.base.data.** { *; }

-dontwarn org.xmlpull.v1.XmlPullParser
-dontwarn android.content.res.XmlResourceParser
-keep class org.xmlpull.v1.** { *; }
-keep class android.content.res.XmlResourceParser { *; }
-keep class org.simpleframework.xml.** { *; }
-keepclassmembers class org.simpleframework.xml.** { *; }

# Keep `INSTANCE.serializer()` of serializable objects.
-keep @kotlinx.serialization.Serializable class * {*;}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class app.editors.manager.ui.dialogs.** { *; }
#-keep class lib.toolkit.base.managers.utils.JsonUtils { *; }
#-keepclassmembers class lib.toolkit.base.managers.utils.JsonUtils { *; }
