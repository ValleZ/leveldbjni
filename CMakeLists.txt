cmake_minimum_required(VERSION 3.17)
project(leveldbjni VERSION 0.1 LANGUAGES CXX)

configure_file(CMakeLists.txt.in leveldb-download/CMakeLists.txt)
execute_process(COMMAND ${CMAKE_COMMAND} -G "${CMAKE_GENERATOR}" .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download )
execute_process(COMMAND ${CMAKE_COMMAND} --build .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-download )

add_subdirectory(${CMAKE_BINARY_DIR}/leveldb-src
                 ${CMAKE_BINARY_DIR}/leveldb-build)
find_library(LEVELDB_LIBRARY NAMES leveldbc PATHS ${CMAKE_BINARY_DIR}/leveldb-build)
message("LEVELDB_LIBRARY = ${LEVELDB_LIBRARY}")


set(CMAKE_CXX_STANDARD 11)
find_package(JNI)
message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
include_directories("${JNI_INCLUDE_DIRS}" "${CMAKE_BINARY_DIR}/leveldb-src/include")
add_library(leveldbjni SHARED LevelDb.cpp)
target_link_libraries(leveldbjni "${LEVELDB_LIBRARY}")
