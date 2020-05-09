#!/bin/bash
export set JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/
javac -h . src/LevelDb.java
g++ -c -fPIC -std=c++11 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -I . Main.cpp -o Main.o
g++ -dynamiclib -o libex.dylib Main.o ./libleveldb.a  -lc
