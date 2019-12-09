Prosys OPC UA SDK for Java can be used for Android application development as well as for normal Java development. 

INSTALLATION

Android Studio is the recommended development environment for Android development. Please see the blog post

https://www.prosysopc.com/blog/developing-opc-ua-applications-in-android-studio/

for an introduction how to setup Android Studio for development with Prosys OPC UA SDK for Java.

LIBRARIES

The libraries that are usable on Android are:

prosys*.jar
slf4j-api-*.jar
lib-android/*.jar

The 'lib-android' folder contains Spongy Castle (https://rtyley.github.io/spongycastle/) security libraries, 
which are necessary for Android applications to support UA security.

Android includes a limited version of Bouncy Castle and the standard Bouncy Castle cannot be installed there. 
However, the Spongy Castle libraries will provide the same functionality as Bouncy Castle in Android, 
so these libraries should be used instead.

SLF4J is the native logging environment for Android. See https://www.slf4j.org/android/ for more about that.

NOTE: It is not possible to use HTTPS on Android due to library conflicts in HTTP Core components.

NOTE: Server development for Android is not possible with the current version due to XML library limitations.

NOTE: Per https://github.com/rtyley/spongycastle/releases/tag/sc-v1.56.0.0, the previously named 'pkix' has now the exactly same name as the Bouncy Castle lib ('bcpkix-jdk15on').
