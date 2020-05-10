#!/bin/bash
export set JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home
export set LEVELDB_DIR=../leveldb
#javac -h . library/src/com/github/vallez/leveldbjni/LevelDb.java
g++ -c -fPIC -std=c++11 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -I ${LEVELDB_DIR}/include LevelDb.cpp -o LevelDb.o
g++ -dynamiclib -o libleveldbjni.dylib LevelDb.o ${LEVELDB_DIR}/build/libleveldb.a  -lc
rm LevelDb.o

