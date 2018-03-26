CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

#project name
PROJECT(TMCDistributor)

SET(CMAKE_Java_COMPILER /usr/bin/javac)

#introot definition
SET(INTROOT $ENV{INTROOT})
IF(NOT INTROOT)
    SET(CMAKE_INSTALL_PREFIX="/opt/TMCS")
    MESSAGE("INTROOT not defined, CMAKE_INSTALL_PREFIX set to: /opt/TMCS")
ELSE (NOT INTROOT)
    SET(CMAKE_INSTALL_PREFIX ${INTROOT})
    MESSAGE("CMAKE_INSTALL_PREFIX set to: $ENV{INTROOT}")
ENDIF (NOT INTROOT)

#delegate to ant to build everything
ADD_CUSTOM_COMMAND(
    OUTPUT ${PROJECT_SOURCE_DIR}/../build/TMCTTArchiver.jar
    COMMAND ${CMAKE_COMMAND} ARGS -E make_directory ${PROJECT_SOURCE_DIR}/../lib
    COMMAND ${CMAKE_COMMAND} ARGS -E chdir ${PROJECT_SOURCE_DIR}/../ 
          /usr/bin/ant  
    COMMAND cp ${PROJECT_SOURCE_DIR}/../build/TMCTTArchiver.jar ${PROJECT_SOURCE_DIR}/../lib/TMCTTArchiver.jar
)

ADD_CUSTOM_TARGET(TMCDistributor_jar ALL
    DEPENDS ${PROJECT_SOURCE_DIR}/../build/TMCTTArchiver.jar
)

#delete generated files in make clean 
SET_DIRECTORY_PROPERTIES (
   PROPERTIES 
     ADDITIONAL_MAKE_CLEAN_FILES "${PROJECT_SOURCE_DIR}/../build"
     ADDITIONAL_MAKE_CLEAN_FILES "${PROJECT_SOURCE_DIR}/../lib/TMCTTArchiver.jar"
)

#install ../lib/TMCTTArchiver.jar
INSTALL(
   FILES ${PROJECT_SOURCE_DIR}/../lib/TMCTTArchiver.jar DESTINATION lib 
)


