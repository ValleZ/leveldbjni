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
execute_process(COMMAND ${CMAKE_COMMAND} -G "${CMAKE_GENERATOR}" .
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
