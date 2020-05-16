#!/bin/bash
#export set JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home
#export set LEVELDB_DIR=./build/leveldb-src
#javac -h . library/src/com/github/vallez/leveldbjni/LevelDb.java
#g++ -c -fPIC -std=c++11 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/darwin -I ${LEVELDB_DIR}/include ./library/cpp/com_github_vallez_leveldbjni_LevelDb.cpp -o LevelDb.o
#g++ -dynamiclib -o libleveldbjni.dylib LevelDb.o ${LEVELDB_DIR}/libleveldb.a  -lc
#rm LevelDb.o

# add set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC") to leveldb when needed

#cmake -G "Visual Studio 16 2019" ..
#cmake --build . --config Release

# docker create -it -v $(pwd):/var/prj arm64v8/debian

#ssh-keygen -t rsa -b 4096 -C "gjmwt@ya.ru"

apt-get update
apt-get install cmake
apt-get install build-essential
apt-get install git
apt-get install default-jdk
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release ..
cmake --build .
