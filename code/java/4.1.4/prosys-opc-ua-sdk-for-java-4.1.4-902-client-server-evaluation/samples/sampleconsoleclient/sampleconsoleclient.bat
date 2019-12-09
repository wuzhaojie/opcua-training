@echo off

rem A sample script that sets the classpath, compiles an application and runs it.

setlocal

set libdir=../../lib
set bindir=./src/main/java
set srcdir=./src/main/java

set CP=%libdir%\*

if not defined JAVA_HOME (
echo JAVA_HOME environment variable must be set!
EXIT /B 1
)

rem Ensure that the path is guarded with hyphens
if exist "%JAVA_HOME%" set JAVA_HOME="%JAVA_HOME%"

if not exist %JAVA_HOME%\bin\javac.exe (
echo could not find 'javac' in %%JAVA_HOME%%\bin\
EXIT /B 2
)

if not exist %bindir%\com\prosysopc\ua\samples\client\SampleConsoleClient.class %JAVA_HOME%\bin\javac -classpath %CP% %srcdir%\com\prosysopc\ua\samples\client\*.java 

%JAVA_HOME%\bin\java -classpath %bindir%;%CP% com.prosysopc.ua.samples.client.SampleConsoleClient %1 %2 %3 %4 %5 %6 %7 %8 %9

:END
endlocal
rem Leave the window open, in case you double clicked the .bat-file in Explorer
if "%1" == "" pause 
EXIT /B 0