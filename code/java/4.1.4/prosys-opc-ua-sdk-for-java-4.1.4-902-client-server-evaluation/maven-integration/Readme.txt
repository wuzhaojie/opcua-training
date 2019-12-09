The folder 'maven-install-helper' contains a Maven project which can be used to install the SDK to your local (.m2) Maven repo/cache.

You will need to run 'mvn install' in the 'maven-install-helper' directory (or via your favorite IDE).

NOTE! SDK contains optional dependencies, please refer to the main README. 

You might need to edit the POM file inside the 'data' if developing on android or wish to remove
the optional libs required for OPC.HTTPS transport protocol.