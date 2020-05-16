cmake_minimum_required(VERSION 3.13)
message (">>>>>>>>>> build JNI wrapper library")
project(leveldbjni VERSION 0.3 LANGUAGES C CXX)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")

message (">>>>>>>>>> download leveldb")
configure_file(CMakeLists.txt.in leveldb-download/CMakeLists.txt)
message ("CMAKE_CXX_FLAGS=${CMAKE_CXX_FLAGS}")
message ("CMAKE_COMMAND=${CMAKE_COMMAND}")
message ("CMAKE_GENERATOR=${CMAKE_GENERATOR}")
message ("CMAKE_BINARY_DIR=${CMAKE_BINARY_DIR}")
message ("CMAKE_SOURCE_DIR=${CMAKE_SOURCE_DIR}")

execute_process(COMMAND ${CMAKE_COMMAND} -G "${CMAKE_GENERATOR}" .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download)
execute_process(COMMAND ${CMAKE_COMMAND} --build .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download)

message (">>>>>>>>>> leveldb downloaded to leveldb-src, now building it")
execute_process(COMMAND ${CMAKE_COMMAND} -DCMAKE_BUILD_TYPE=Release .
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
message ("CMAKE_SHARED_LIBRARY_SUFFIX=${CMAKE_SHARED_LIBRARY_SUFFIX}")

FILE(MAKE_DIRECTORY "${CMAKE_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}")
ADD_CUSTOM_COMMAND(TARGET leveldbjni POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy  ${CMAKE_BINARY_DIR}/libleveldbjni${CMAKE_SHARED_LIBRARY_SUFFIX}
          ${CMAKE_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}/libleveldbjni${CMAKE_SHARED_LIBRARY_SUFFIX}
        COMMAND "${Java_JAVAC_EXECUTABLE}" -d native_jar ${CMAKE_SOURCE_DIR}/library/src/com/github/vallez/leveldbjni/LevelDb.java
        COMMAND "${Java_JAR_EXECUTABLE}" -cfM leveldbjni.jar -C native_jar .
    )
