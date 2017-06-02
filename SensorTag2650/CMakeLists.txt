cmake_minimum_required(VERSION 2.8.3)
project(iot)

include_directories(
  ${PROJECT_SOURCE_DIR}/include/)

#SensorTag executable
SET(SENSORTAG_SOURCES ${PROJECT_SOURCE_DIR}/src/sensortag.cpp)

add_executable(sensortag ${SENSORTAG_SOURCES})







