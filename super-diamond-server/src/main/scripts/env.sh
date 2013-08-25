#!/bin/bash

#JAVA_HOME="/usr/java/jdk1.6.0_33"

if [ "$JAVA_HOME" != "" ]; then
  #echo "run java in $JAVA_HOME"
  JAVA_HOME=$JAVA_HOME
fi

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java
BASE_HOME=$BASE_DIR
SERVER_NAME="ConfigServer"
JETTY_PORT="8080"
STARTUP_CLASS="com.ustcinfo.tpc.config.jetty.ServerStartup"

#Ueap JMX port
export JMX_PORT=9123
export CLASSPATH=$BASE_DIR/webapp/WEB-INF/classes:$(ls $BASE_DIR/extra-lib/*.jar | tr '\n' :)

#UEAP jvm args
BASE_JVM_ARGS="-Xmx512m -Xms256m -server"
BASE_JVM_ARGS="$BASE_JVM_ARGS -cp $CLASSPATH -Dbase.home=$BASE_HOME -Dserver.name=$SERVER_NAME -Djetty.port=$JETTY_PORT"