#!/bin/bash

if [ -z "$BASE_DIR" ] ; then
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done
  BASE_DIR=`dirname "$PRG"`/..

  # make it fully qualified
  BASE_DIR=`cd "$BASE_DIR" && pwd`
  #echo "collect master is at $BASE_DIR"
fi

source $BASE_DIR/bin/env.sh

AS_USER=`whoami`
LOG_DIR="$BASE_DIR/logs"
LOG_FILE="$LOG_DIR/server.log"
PID_DIR="$BASE_DIR/logs"
PID_FILE="$PID_DIR/.run.pid"

function running(){
	if [ -f "$PID_FILE" ]; then
		pid=$(cat "$PID_FILE")
		process=`ps aux | grep " $pid " | grep -v grep`;
		if [ "$process" == "" ]; then
	    	return 1;
		else
			return 0;
		fi
	else
		return 1
	fi	
}

function start_server() {
	if running; then
		echo "$SERVER_NAME is running."
		exit 1
	fi

    mkdir -p $PID_DIR
    mkdir -p $LOG_DIR
    touch $LOG_FILE
    chown -R $AS_USER $PID_DIR
    chown -R $AS_USER $LOG_DIR
    
    echo "$JAVA $APP_JVM_ARGS -DBASE_HOME=$BASE_HOME -DSERVER_NAME=$SERVER_NAME -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
   	  -Dcom.sun.management.jmxremote.port=$JMX_PORT -DSERVER_NAME=$SERVER_NAME $BASE_APP_ARGS com.github.runner.ServerStartup"
    sleep 1
    nohup $JAVA $APP_JVM_ARGS -DBASE_HOME=$BASE_HOME -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
   	  -Dcom.sun.management.jmxremote.port=$JMX_PORT $BASE_APP_ARGS com.github.runner.ServerStartup 2>&1 >>$LOG_FILE &	
    echo $! > $PID_FILE
    
    chmod 755 $PID_FILE
  	sleep 1;
    tail -f $LOG_FILE
}

function stop_server() {
	if ! running; then
		echo "$SERVER_NAME is not running."
		exit 1
	fi
	count=0
	pid=$(cat $PID_FILE)
	while running;
	do
	  let count=$count+1
	  echo "Stopping $SERVER_NAME $count times"
	  if [ $count -gt 5 ]; then
	  	  echo "kill -9 $pid"
	      kill -9 $pid
	  else
	  	  echo "$JAVA -DBASE_HOME=$BASE_HOME -Dhost=127.0.0.1 -Dport=$JMX_PORT com.github.runner.StopServerTool"
    	  sleep 1
	      $JAVA -DBASE_HOME=$BASE_HOME -Dhost=127.0.0.1 -Dport=$JMX_PORT com.github.runner.StopServerTool $@
	      kill $pid
	  fi
	  sleep 3;
	done	
	echo "Stop $SERVER_NAME successfully." 
	rm $PID_FILE
}

function status(){
    if running; then
       echo "$SERVER_NAME is running.";
       exit 0;
    else
       echo "$SERVER_NAME was stopped.";
       exit 1;
    fi
}

function reload_logback_config() {
    if ! running; then
        echo "$SERVER_NAME is not running."
        exit 1  
    fi  
    echo "$JAVA -DBASE_HOME=$BASE_HOME -Dhost=127.0.0.1 -Dport=$JMX_PORT com.github.runner.ReloadLogbackConfig"
    sleep 1
    $JAVA -DBASE_HOME=$BASE_HOME -Dhost=127.0.0.1 -Dport=$JMX_PORT com.github.runner.ReloadLogbackConfig $@
}
 
function help() {
    echo "Usage: server.sh {start|status|stop|restart|logback}" >&2
    echo "       start:             start the $SERVER_NAME server"
    echo "       stop:              stop the $SERVER_NAME server"
    echo "       restart:           restart the $SERVER_NAME server"
    echo "       logback:           reload logback config file"
    echo "       status:            get $SERVER_NAME current status,running or stopped."
}

command=$1
shift 1
case $command in
    start)
        start_server $@;
        ;;    
    stop)
        stop_server $@;
        ;;
    logback)
        reload_logback_config $@;
        ;;
    status)
    	status $@;
        ;; 
    restart)
        $0 stop $@
        $0 start $@
        ;;
    help)
        help;
        ;;
    *)
        help;
        exit 1;
        ;;
esac