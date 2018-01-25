#!/bin/bash
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2009 
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
# "@(#) $Id: runPersistenceUnitTest.sh,v 1.4 2013/03/17 15:01:29 tstaig Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# pburgos  2009-04-05  created
#

export PYTHONPATH=../lib/python/site-packages:$PYTHONPATH

declare TEST_SCRIPT="`which ant` "
declare TEST_SUITE=1
declare TEST_LOG=/dev/stdout

if test $# -ge 1; then
  TEST_SUITE=$1
  if test $# -eq 2; then
    TEST_LOG=$2
  fi
fi


declare TEST_TMP=tmp
if [ -d $TEST_TMP ]; then
touch tmp/PersistenceUnitTest.log
else
#echo "Error: '$check' does not exist!!"
#echo "EXITING"
#exit 1
mkdir $TEST_TMP
touch $TEST_TMP/PersistenceUnitTest.log
fi

$TEST_SCRIPT init  killHSQLDB startHSQLDB createHSQLDBTables copymetafiles &> $TEST_LOG


#acsStartjava -jar $ACSROOT/lib/hsqldb.jar --rcFile resources/hsqldb/sqltool.rc --sql "SHUTDOWN;" localhost-sa
#
#acsStartJava org.hsqldb.Server -database.0
#			<arg value="tmcdb" />
#			<arg value="-dbname.0" />
#			<arg value="tmcdb" />
#sleep 5

export JAVA_OPTIONS="-Darchive.configFile=archiveConfig.properties"
acsStartJava org.testng.TestNG resources/TestNG/testng.xml >> $TEST_LOG 2>&1

$TEST_SCRIPT stopHSQLDB >> $TEST_LOG 2>&1

declare TEST_RESULT="`grep -R FAIL ../test/test-output/testng-results.xml`" 
#RESULT=$?
#if [ "$RESULT" = "0" ]; then
#    printf "OK\n"
#else
#    printf "ERROR\n"
#fi

if [ "$TEST_RESULT" = "" ]; then
    printf "OK\n"
else
    printf "ERROR\n"
fi


# __pBa__
