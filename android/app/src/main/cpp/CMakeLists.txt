cmake_minimum_required(VERSION 3.18.1)

project("flutter_jni_poc")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(
    ${ANDROID_NDK}/sysroot/usr/include
    ${ANDROID_NDK}/sysroot/usr/include/${ANDROID_SYSROOT_ABI}
)

add_library(
        native_lib
        SHARED
        native_lib.cc
)

target_link_libraries(
        native_lib
        android
        log
)
