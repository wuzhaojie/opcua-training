This is the distribution of Prosys OPC UA SDK for Java.

See the LICENSE.txt document ('license' folder) for license information.

The SDK requires Java SE 6 (Java Runtime Environment 1.6) or later. It is tested with Java SE 6, 7, 8 and 11*.

You also need to install JDK 6 (jdk1.6) or later to be able to compile your applications. 
Obtaining one is outside the scope of the SDK, but e.g. https://adoptopenjdk.net/ could be used.

(* Not tested in the "module path" mode of java 11. The SDK jar does not contain a module-info file
and might not work with tools like jlink)

INSTALL
 
Add the libraries provided in the 'lib'-directory of the SDK distribution package to the Java classpath 

Some of the libraries are optional, please see DEPLOYMENT section of this README for more information.
in your environment. You can then use them in your applications.

See the Starting Guide in the 'tutorial' folder of the SDK distribution package for tips and 
step-by-step instructions on setting up the SDK for use in your application development. 


SAMPLES

It is recommended that you import the samples to a proper development environment (see the Starting Guide).

However, you can also launch each sample using the provided batch or shell script in the sample folder, 
assuming that you have installed the JDK and set up the JAVA_HOME environment variable to the JDK directory. 
The Java compiler should be located at %JAVA_HOME%\bin\javac.exe (Windows) or $JAVA_HOME/bin/javac (Linux).  


TUTORIALS

The tutorials for client and server application development are found in the 'tutorial' folder 
of the SDK distribution package and will help you get started with using the SDK.


DEPLOYMENT

When you deploy your applications, you will also need to deploy the respective libraries. 
But you won't necessarily need them all. The table below lists the available libraries with descriptions and
 use cases: 

(lib folder)
prosys*              SDK classes             Necessary for all applications
slf4j-api-*.jar      SLF4J Logging Facade	 Necessary for all applications
slf4j-log4j12-*.jar  SLF4J-to-Log4J bridge   If you wish to use Log4J version 1.2 for logging (see below for more)
log4j-1.2.17.jar     Apache Log4J            If you wish to use Log4J version 1.2 for logging
http-*.jar           HTTP Core components    Necessary for all applications that support HTTPS
commons-logging*.jar Commons Logging         Necessary for all client applications that support HTTPS
bc*.jar              Bouncy Castle security  In practice necessary for Java SE for OPC UA Security support

(lib-android folder)
*.jar                Spongy Castle security  Necessary for Android applications to support OPC UA Security.


You should bundle the SDK classes to your own application (to create a 'merged' or 'uber' .jar out of it).
Obfuscation techniques are also recommended to guard it against illegal usage. ProGuard is a recommended tool,
for example.

NOTE! The Bouncy Castle library must not be repackaged into your application. 
It is a signed security library and must be linked to your application via the original bc*.jar
files in the classpath, otherwise it will not work.

Also note the respective licenses for each library, as mentioned in LICENSE.txt


USAGE OF SECURITY LIBRARIES

OPC UA Security is implemented in the "stack layer" of the SDK. The Stack will pick 
an available security library automatically, but if necessary, even a custom library can be plugged in.

Security libraries are used as they are available. Testing has so far based on the Bouncy Castle 
library and it is therefore recommended in normal applications.

The current implementation, is based on a flexible CryptoProvider model. 
There are several implementations of this interface available in the stack and you 
can also implement your own, if you have a custom security framework that you need to use.

The stack will pick a default CryptoProvider automatically as it finds them from the class
path. Alternatively, you can define the provider that you wish to use by setting it with 
CryptoUtil.setCryptoProvider() or with CryptoUtil.setSecurityPoviderName(). 

In the same manner, custom certificate framework can be used by implementing interface CertificateProvider 
and setting it with CertificateUtils.setCertificateProvider().

Current CryptoProvider implementations:

BcCryptoProvider (default, if Bouncy Castle is in the class path: uses Bouncy Castle directly)
ScCryptoProvider (default in Android, if Spongy Castle is in the class path)
BcJceCryptoProvider (uses Bouncy Castle via the JCE crypto framework)
ScJceCryptoProvider (uses Spongy Castle via the JCE crypto framework)

If any of the ...JceCryptoProvider is used, depending on your JVM provider, you might need to 
install the JCE Unlimited Strength Jurisdiction Policy Files. For Oracle JVM (for Java 6, 7 or 8, respectively), 
to enable support for 256 bit security policies:

JRE6: http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html
JRE7: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
JRE8: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

Android includes a limited version of Bouncy Castle and the standard Bouncy Castle cannot 
be installed there. It also does not include the Sun classes. However, the Spongy Castle
libraries will provide the same functionality as Bouncy Castle in Android, so these 
libraries should be used in Android, unless the application can do without security altogether.

Current CertificateProvider implementations:

BcCertificateProvider (default, if Bouncy Castle is in the class path)
ScCertificateProvider (default in Android, if Spongy Castle is in the class path)


LOGGING LIBRARY SELECTION

The SDK uses SLF4J logging facade for message logging. This enables each application
to use any specific logging library because SLF4J is just used to direct logging to a selected library.

'slf4j-api' must always be included in the application build path.

The actual library selection is done by picking one of the SLF4J bridges and the actual library. 
The sample applications of the SDK use log4j, so it is included in the 'lib' folder - and therefore the 
'slf4j-log4j12' bridge is also included. For other logging alternatives, see http://slf4j.org.


ANDROID DEVELOPMENT

See README-android.txt

CODE GENERATION

The SDK contains the Code Generator, which can be used to create Java classes based on various OPC UA 
type definitions provided in information models (in the NodeSet2 XML format). The Code Generator is in 
the 'codegen' directory of the SDK package. See the manual in there for more details on how to use it. 


KNOWN ISSUES

Following lists of some known issues:

- TLS 1.2 policy required by OPC UA does not work (required ciphers not supported by JSSE) 
- TLS 1.2 with PFS is supported and is currently the only one suggested by OPC Foundation. For interoperability issues
  the other TLS versions are still included in the default configuration of the samples, for example.
- HTTPS testing is not finished yet with the other stacks and interoperability depends on the availability of
  the different TLS versions
- .NET Client requires that the server has a certificate signed by a trusted CA, if HTTPS is used. Therefore
  the sample applications create their own CA certificate which they use to sign the HTTPS certificates.


MIGRATION FROM 3.x

If you are migrating an existing application that is using version 3.x of the Prosys OPC UA SDK for Java, you can check the Migration Guide 
available in the 'tutorial' folder of the SDK distribution package. It explains the differences in 4.x and helps you to modify your application respectively.


Copyright (c)
Prosys OPC Ltd.
http://www.prosysopc.com
uajava-support@prosysopc.com