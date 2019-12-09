#!/bin/sh

libdir=../../lib
bindir=./src/main/java
srcdir=./src/main/java

CP="$libdir/*"

test -e $bindir/com/prosysopc/ua/samples/server/SimpleServer.class || javac -classpath "$CP" $srcdir/com/prosysopc/ua/samples/server/*.java

java -classpath $bindir:"$CP" com.prosysopc.ua.samples.server.SimpleServer $* 