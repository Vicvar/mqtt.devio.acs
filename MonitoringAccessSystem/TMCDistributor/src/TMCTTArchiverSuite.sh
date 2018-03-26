#! /bin/bash

export LD_LIBRARY_PATH=/home/tmc/TimesTen/ttTMCS/lib
export CLASSPATH=/introots/TMCS/lib/TMCTTArchiver.jar:/introots/TMCS/lib/TMCStats.jar:/introots/TMCS/lib/ttjdbc6.jar:/introots/TMCS/lib/activemq-all-5.4.1.jar:/introots/TMCS/lib/castor.jar:/introots/TMCS/lib/TMCDBUtils.jar:/introots/TMCS/lib/archive_database.jar:/introots/TMCS/lib/hibernate3.jar:/introots/TMCS/lib/TMCDBPersistence.jar:/introots/TMCS/lib/xercesImpl.jar:/introots/TMCS/lib/jedis-2.1.0.jar:/introots/TMCS/lib/commons-pool-1.6.jar:/introots/TMCS/lib/log4j-1.2.17.jar:/introots/TMCS/lib/junit-4.10.jar:/introots/TMCS/lib/indexed-file-reader-java6-1.0.jar:/introots/TMCS/lib/jsr166y-1.7.0.jar:$CLASSPATH

java ${JAVA_OPTS} archive.tmcdb.monitoring.TMCOffline.test.TMCTTArchiverRunner
