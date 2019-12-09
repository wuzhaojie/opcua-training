This is the distribution of Prosys OPC UA Java SDK.

See the LICENSE document for license information.

SYSTEM REQUIREMENTS

The Java stack requires Java SE 6 (Java Runtime Environment 1.6). 
You also need to install JDK 6 (jdk1.6) to be able to compile your applications.
The downloads are available from 
http://java.sun.com/javase/6/

INSTALL

Extract the SDK Package on your computer. 
Add the libraries in the 'lib'-directory to the Java classpath in your environment. You
can then use them in your applications.

The samples in the package assume that you have installed JDK and set up the
JAVA_HOME environment variable to the JDK directory 
(so that the Java compiler is located at %JAVA_HOME%\bin\javac.exe (Windows) 
or $JAVA_HOME/bin/javac (Linux))  

If your setup works, you can run the sample using the batch or shell script 
in the samples directory.

INSTALL IN ECLIPSE

If you are running Eclipse, you can install the SDK in it as follows:

- Go to the Package Explorer on the left.
- Select 'New-Java Project'
- Select 'Create project from existing source' and find the directory
  where you extracted the package
- Press Finish

=> You should have the project in your workspace.

To use the SDK in your own projects, you can simply copy all the .jar:s and 
add them to your project's Build Path. They should appear in your project's
Referenced Libraries, after that. 

JAVADOC INSTALL 

The Javadoc of the SDK is zipped in the doc-directory. You can install it to Eclipse 
as zipped or extracted.

Find the SDK.jar from your project's Referenced Libraries (see above) and open
Properties for it. There you can locate the javadoc and link it for the SDK library.

You can do the same with the Opc.Ua.Stack.jar. 

SOURCE INSTALL 

If you have the source version of the SDK, you can find it zipped in the src directory.

If you did not unzip the source before you installed it to Eclipse, 
you can install the source as follows:

Unzip the client-src package in the src directory and also this server source in there.

In Eclipse, define that src folder is a source folder:

- In Package Explorer select 'src' folder
- From the popup menu (right click), select 'Build Path->Use as Source Folder'
- You may also need to refresh the Package Explorer to see the extracted source files: Press F5.

Do the same with server-folder.

To test that everything works, go to the samples-folder in Eclipse and 
locate the SampleConsoleServer. Find 'Run As-Java Application' from the popup menu to start it up. 

Copyright (c) 2010 
Prosys PMS Ltd
http://www.prosysopc.com
uajava-support@prosysopc.com
