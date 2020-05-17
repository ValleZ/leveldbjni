#
#  Copyright (C) 2020 Valentin Konovalov <valle.ketsujin@gmail.com>
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as
#  published by the Free Software Foundation, either version 3 of the
#  License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
cmake_minimum_required(VERSION 3.13)
message (">>>>>>>>>> build JNI wrapper library")
project(leveldbjni VERSION 1.122.0 LANGUAGES C CXX)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
if (WIN32)
  set(LIB_TARGET_DIR "Release/")
else (WIN32)
  set(LIB_TARGET_DIR "")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif (WIN32)


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

message (">>>>>>>>>> leveldb downloaded to leveldb-src, now generating build files")
execute_process(COMMAND ${CMAKE_COMMAND} -E env CXXFLAGS="-fPIC" ${CMAKE_COMMAND} -DCMAKE_BUILD_TYPE=Release .
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-src)
message (">>>>>>>>>> leveldb build")
execute_process(COMMAND ${CMAKE_COMMAND} --build . --config Release --target leveldb
  WORKING_DIRECTORY ${CMAKE_BINARY_DIR}/leveldb-src)
message (">>>>>>>>>> leveldb build finished")
find_library(LEVELDB_LIBRARY NAMES leveldb PATHS ${CMAKE_BINARY_DIR}/leveldb-src
  ${CMAKE_BINARY_DIR}/leveldb-src/Release)
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
message ("CMAKE_SYSTEM_PROCESSOR=${CMAKE_SYSTEM_PROCESSOR}")
message ("Java_JAR_EXECUTABLE=${Java_JAR_EXECUTABLE}")
message ("CMAKE_SHARED_LIBRARY_SUFFIX=${CMAKE_SHARED_LIBRARY_SUFFIX}")
message ("CMAKE_SHARED_LIBRARY_PREFIX=${CMAKE_SHARED_LIBRARY_PREFIX}")


FILE(MAKE_DIRECTORY "${CMAKE_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}/${CMAKE_SYSTEM_PROCESSOR}")
ADD_CUSTOM_COMMAND(TARGET leveldbjni POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy  ${CMAKE_BINARY_DIR}/${LIB_TARGET_DIR}${CMAKE_SHARED_LIBRARY_PREFIX}leveldbjni${CMAKE_SHARED_LIBRARY_SUFFIX}
          ${CMAKE_BINARY_DIR}/native_jar/META-INF/native/${CMAKE_SYSTEM_NAME}/${CMAKE_SYSTEM_PROCESSOR}/${CMAKE_SHARED_LIBRARY_PREFIX}leveldbjni${CMAKE_SHARED_LIBRARY_SUFFIX}
        COMMAND "${Java_JAVAC_EXECUTABLE}" -d native_jar ${CMAKE_SOURCE_DIR}/library/src/com/github/vallez/leveldbjni/LevelDb.java
        COMMAND "${Java_JAR_EXECUTABLE}" -cfM leveldbjni.jar -C native_jar .
    )
