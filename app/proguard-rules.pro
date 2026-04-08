-dontobfuscate
-keep class com.hujiayucc.hook.ModuleMain { *; }
-keep class * extends com.hujiayucc.hook.annotation.* { *; }
-dontwarn **

-keepattributes *Annotation*
-keep class androidx.tracing.** { *; }
-keep class androidx.core.math.MathUtils { *; }