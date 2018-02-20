#! /bin/bash

export CONFIG_ARCHIVER_FILE=/opt/tmcs/TMCS/config/config_archiver.properties

export CLASSPATH=/opt/tmcs/TMCS/lib/TMCTTArchiver.jar:/opt/tmcs/TMCS/lib/TMCStats.jar:/opt/tmcs/TMCS/lib/activemq-all-5.6.0.jar:/opt/tmcs/TMCS/lib/castor.jar:/opt/tmcs/TMCS/lib/TMCDBUtils.jar:/opt/tmcs/TMCS/lib/archive_database.jar:/opt/tmcs/TMCS/lib/hibernate3.jar:/opt/tmcs/TMCS/lib/TMCDBPersistence.jar:/opt/tmcs/TMCS/lib/xercesImpl.jar:/opt/tmcs/TMCS/lib/jedis-2.1.0.jar:/opt/tmcs/TMCS/lib/commons-pool-1.6.jar:/opt/tmcs/TMCS/lib/log4j-1.2.17.jar:/opt/tmcs/TMCS/lib/commons-logging-1.1.1.jar:/opt/tmcs/TMCS/lib/spring-beans-3.2.18.RELEASE.jar:/opt/tmcs/TMCS/lib/spring-context-3.2.18.RELEASE.jar:/opt/tmcs/TMCS/lib/spring-core-3.2.18.RELEASE.jar:/opt/tmcs/TMCS/lib/spring-expression-3.2.18.RELEASE.jar:/opt/tmcs/TMCS/lib/TMCAgent.jar:/opt/tmcs/TMCS/lib/indexed-file-reader-java6-1.0.jar:/opt/tmcs/TMCS/lib/jsr166y-1.7.0.jar:/opt/tmcs/TMCS/lib/influxdb-java-2.8.jar:/opt/tmcs/TMCS/lib/converter-moshi-2.3.0.jar:/opt/tmcs/TMCS/lib/okhttp-3.0.0-RC1.jar:/opt/tmcs/TMCS/lib/retrofit-2.3.0.jar:/opt/tmcs/TMCS/lib/logging-interceptor-3.3.1.jar:/opt/tmcs/TMCS/lib/moshi-1.5.0.jar:/opt/tmcs/TMCS/lib/okio-1.13.0.jar:$CLASSPATH

JAVA_OPTS="-Djava.rmi.server.hostname=`hostname -f`"
JAVA_OPTS="${JAVA_OPTS} -Dlog4j.debug -Dlog4j.configuration=file:/opt/tmcs/TMCS/config/log4j.xml"
JAVA_OPTS="${JAVA_OPTS} -Xms5000m -Xmx5000m"
JAVA_OPTS="${JAVA_OPTS} -Denv=Dev"

export JAVA_OPTS

java ${JAVA_OPTS} archive.tmcdb.monitoring.TMCOffline.TMCTTArchiverClient
