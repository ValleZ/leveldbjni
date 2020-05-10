#!/bin/bash
export set JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/
javac -h . src/LevelDb.java
g++ -c -fPIC -std=c++11 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -I . LevelDb.cpp -o LevelDb.o
g++ -dynamiclib -o libleveldbjni.dylib LevelDb.o ./libleveldb.a  -lc
