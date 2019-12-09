    OPC UA Java Stack Source and Sample Applications package instructions
                
1. Introduction

This document contains release notes, and build and compile instructions to 
OPC UA Java Stack source package.

Change Log:
  1.00 - Mar 19, 09 - Document Created.
  1.01 - Nov 15, 09 - 
  1.02 - Jun 17, 13
  
2. Package file structure description

src/
    
    org/opcfoundation/ua/
    
        This folder contains all of the stack source code.

    org/opcfoundation/ua/application
    
        The code in this folder is for application developer.
        The main classes here are Client and Server. 
    
    org/opcfoundation/ua/builtintypes
    
        This package contains classes for UA builtin types. 
        The following table describes the mapping between Java classes
        and UA Builtin types:
        
        1     Boolean           java.lang.Boolean 
        2     SByte             java.lang.Byte 
        3     Byte              org.opcfoundation.ua.builtintypes.UnsignedByte 
        4     Int16             java.lang.Short 
        5     UInt16            org.opcfoundation.ua.builtintypes.UnsignedShort 
        6     Int32             java.lang.Integer 
        7     UInt32            org.opcfoundation.ua.builtintypes.UnsignedInteger 
        8     Int64             java.lang.Long 
        9     UInt64            org.opcfoundation.ua.builtintypes.UnsignedLong 
        10    Float             java.lang.Float 
        11    Double            java.lang.Double 
        12    String            java.lang.String 
        13    DateTime          org.opcfoundation.ua.builtintypes.DateTime 
        14    Guid              java.util.UUID 
        15    ByteString        byte[] 
        16    XmlElement        org.opcfoundation.ua.builtintypes.XmlElement 
        17    NodeId            org.opcfoundation.ua.builtintypes.NodeId 
        18    ExpandedNodeId    org.opcfoundation.ua.builtintypes.ExpandedNodeId 
        19    StatusCode        org.opcfoundation.ua.builtintypes.StatusCode 
        20    QualifiedName     org.opcfoundation.ua.builtintypes.QualifiedName 
        21    LocalizedText     org.opcfoundation.ua.builtintypes.LocalizedText 
        22    ExtensionObject   org.opcfoundation.ua.builtintypes.ExtensionObject 
        23    DataValue         org.opcfoundation.ua.builtintypes.DataValue 
        24    Variant           org.opcfoundation.ua.builtintypes.Variant 
        25    DiagnosticInfo    org.opcfoundation.ua.builtintypes.DiagnosticInfo 
    
    org/opcfoundation/ua/common
    
        This package contains common and shared classes.
    
    org/opcfoundation/ua/core

        All the code in this folder is "org/opcfoundation/ua/core" codegenerated.
        See (3. Codegenerator) for instructions how to build the codegenerated 
        code. 

    org/opcfoundation/ua/encoding
    
        This folder contains serialization interfaces and serialization 
        implementations.
        
    org/opcfoundation/ua/encoding/binary
    
        The implementation of binary serialization.
    
    org/opcfoundation/ua/encoding/xml
    
        The implementation of xml serialization. (TODO)
    
    org/opcfoundation/ua/transport
    
        This package contains SecureChannel and related interfaces.
        
    org/opcfoundation/ua/transport/tcp
    
        Code for creating TCP/IP based secure channel.
         
    org/opcfoundation/ua/transport/soap
    
        Code for creating a SOAP based secure channel. (TODO)
        
    org/opcfoundation/ua/transport/security
    
        This folder contains common security related classes.
    
    org/opcfoundation/ua/utils
    
        Tons of utility classes.

    org/opcfoundation/ua/utils/asyncsocket
    
        This folder contains a small library that enables asynchronous
        and event based socket operations. The library is based on java NIO.
        
unittests/

    This folder contains unit tests. The tests require JUnit library.
    
examples/

    All stack example sources codes are in this folder.
    
    BigCertificateExample - demonstrates a server and a client with both binary opc.tcp and https communications.
    ClientExample* - demonstrate communications to servers
    ClientAndServerExample - demonstrates a server and a client
    ServerExample1 - deomnstrates a base of a server
    SampleClient - deomnstrates a base of a client
    
codegen/

    All code generator source codes are in this folder.
    See (3. Codegenerator) for instructions how to build source codes.  

codegen_data/

    This folder contains the source data for the code generator .
     
    data/
    
        Type definitions and constants from Model Designer. 

    templates/
    
        Stub template files.
        
    overrides/
    
        This folder contains exceptions to template based files. 
                
testharness/

     Test harness is a test client & server pair that make extensive
     primitive serialization test using random test data.      
     See (5. Testharness) about instructions how to run the test harness.
  
Opc.Ua.Random.JNI/

    RandomGenerator is a random UA builtintype object generator.
    The testharness uses the RandomGenerator from the C-stack
    source codes. 
     
    This folder contains the JNI source codes that wrap the 
    C-code based RandomGenerator.
        
lib/

    This folder contains Java libraries used in the stack, 
    test harness, unit tests and the code generator.
        
build/
    
    All release files generated by the "build.xml" are placed in to this
    folder. The folder is created and deleted by this script. It is not
    intended to be commited into the source control and thus is configured
    to be ignored by SVN. See (4. Creating a release ) to create a build.
    
legal/

    This folder contains LICENSE files of all dependency libraries. 
            
build.xml

    Ant script for creating a release of the stack.
    The main target is "clean-build".
    See (4. Creating a release) for instructions how to create a release.

OPC_UA_JAVA_SDK.htm

    The instruction and version notes document of the release.
    
Readme.txt

    This readme.txt

tickets.txt

    This document contains known issues, bugs and TODO lists of the developers.
    This document is not intended to be released with the stack.
        
3. Codegenerator

This chapter contains instructions how to build code generated java source 
files.

The main class to run is codegen/org.opcfoundation.ua.codegen.Main2.
The generator reads descriptions in codegen_data/ and writes the output
source files to src/org.opcfoundation.ua.core.

The input data is located in these folders:

codegen_data/data/ contains data type descriptions and constants from ModelDesigner.
codegen_data/templates/ contains Java source code templates.
codegen_data/overrides/ contains exceptions to templates.

Note: In eclipse, remeber to refresh (F5) the source tree. 

4. Creating a release 

This chapter contains instructions how to create a release.

Run build.xml. The default target is "clean-build".

    To run the script from command line run "ant build.xml" 
    (See http://ant.apache.org/). 

    To run the script from Eclipse, right click on the build.xml file, and choose
    from context menu "Run As / Ant Build".

To change the version numbering of the release files, see "version" variable 
in build.xml. 

The script creates "build/" folder and places the following release files there:

    OpcUa-xxx-Examples.zip
    
        This archive file contains the stack examples.
    
    OpcUa-xxx-libs.zip
    
        This file contains the dependency libraries of the stack
        library. 
        
    OpcUa-xxx-Project.zip
    
        This file contains the whole source package of this project.

    OpcUa-xxx.jar
    
        This file contains the OPC UA Java Stack -library.
    
    OpcUa-xxx-src.zip
    
        This file contains the source codes to the OPC UA Java Stack -library.
    
    OpcUa-xxx-SDK.zip
    
        This file is actual the SDK release file.
        It contains the following files:
        
            OPC_UA_Java_SDK.html - Release notes 
            OpcUa-xxx.jar - Stack library
            OpcUa-xxx-Examples.zip - Examples
            OpcUa-xxx-src.zip - The library source codes.        
        
5. Testharness

This chapter contains instructions how to run the test harness.

Test Harness requires a server and a client. Run these two main classes: 
 1. org.opcfoundation.ua.server.TestServer
 2. org.opcfoundation.ua.stacktest.client.TestClient
 
The harness executes test cases defined in TestCases.xml and outputs
the results into TestLog.xml and ClientTestLog.xml.

6. Known issues

You must install the JCE Unlimited Strength Jurisdiction Policy Files to 
get support for 256-bit encryption. These ara available from

http://www.oracle.com/technetwork/java/javase/downloads/index.html

TLS 1.2 policy required by OPC UA does not work (not supported by JSSE) 

http://www.opcfoundation.org/mantis
