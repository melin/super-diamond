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
SERVER_NAME="SuperDiamondServer"

#Ueap JMX port
export JMX_PORT=4001
export CLASSPATH=$BASE_DIR/conf:$(ls $BASE_DIR/lib/*.jar | tr '\n' :)

#UEAP jvm args
BASE_APP_ARGS=""
BASE_JVM_ARGS="-Xmx512m -Xms256m -server"
APP_JVM_ARGS="$BASE_JVM_ARGS -cp $CLASSPATH"