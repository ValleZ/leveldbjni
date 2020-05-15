cmake_minimum_required(VERSION 3.17)
message (">>>>>>>>>> build JNI wrapper library")
project(leveldbjni VERSION 0.2 LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 11)

message (">>>>>>>>>> download leveldb")
configure_file(CMakeLists.txt.in leveldb-download/CMakeLists.txt)
message ("CMAKE_COMMAND=${CMAKE_COMMAND}")
message ("CMAKE_GENERATOR=${CMAKE_GENERATOR}")
message ("CMAKE_BINARY_DIR=${CMAKE_BINARY_DIR}")
execute_process(COMMAND ${CMAKE_COMMAND} -G "${CMAKE_GENERATOR}" .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download)
execute_process(COMMAND ${CMAKE_COMMAND} --build .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download)

message (">>>>>>>>>> leveldb downloaded to leveldb-src, now building it")
execute_process(COMMAND ${CMAKE_COMMAND} -G "${CMAKE_GENERATOR}" -DCMAKE_BUILD_TYPE=Release .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-src)
execute_process(COMMAND ${CMAKE_COMMAND} --build . --target leveldb
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-src)

find_library(LEVELDB_LIBRARY NAMES leveldb PATHS ${CMAKE_BINARY_DIR}/leveldb-src)
message("LEVELDB_LIBRARY = ${LEVELDB_LIBRARY}")
find_package(JNI)
message ("JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
include_directories("${JNI_INCLUDE_DIRS}" "${CMAKE_BINARY_DIR}/leveldb-src/include")
add_library(leveldbjni SHARED library/cpp/com_github_vallez_leveldbjni_LevelDb.cpp)
target_link_libraries(leveldbjni "${LEVELDB_LIBRARY}")

#assemble Jar file
find_package(Java REQUIRED)
include(UseJava)
project (leveldbjni-jar NONE)
set(CMAKE_JAVA_COMPILE_FLAGS "-source" "1.8" "-target" "1.8")
set(CMAKE_JNI_TARGET TRUE)

message ("CMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME}")
message ("Java_JAR_EXECUTABLE=${Java_JAR_EXECUTABLE}")

FILE(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}")
ADD_CUSTOM_COMMAND(TARGET leveldbjni POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy  ${CMAKE_CURRENT_BINARY_DIR}/libleveldbjni.dylib
          ${CMAKE_CURRENT_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}/libleveldbjni.dylib
        COMMAND "${Java_JAR_EXECUTABLE}" -cfM native.jar -C native_jar .
    )


add_jar(leveldbjni-jar
  SOURCES library/src/com/github/vallez/leveldbjni/LevelDb.java
  OUTPUT_NAME leveldbjni
  MANIFEST ${CMAKE_BINARY_DIR}/../MANIFEST.MF
  INCLUDE_JARS ${CMAKE_CURRENT_BINARY_DIR}/native.jar)
