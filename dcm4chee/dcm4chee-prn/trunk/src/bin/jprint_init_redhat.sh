#!/bin/sh
#
# jPrint Control Script
#
# chkconfig: 3 80 20
# description: jPrint EJB Container
# 
# To use this script
# run it as root - it will switch to the specified user
# It loses all console output - use the log.
#
# Here is a little (and extremely primitive) 
# startup/shutdown script for RedHat systems. It assumes 
# that jPrint lives in /usr/local/jprint, it's run by user 
# 'jprint' and JDK binaries are in /usr/local/java/bin. All 
# this can be changed in the script itself. 
# Bojan 
#
# Either amend this script for your requirements
# or just ensure that the following variables are set correctly 
# before calling the script

# [ #420297 ] jPrint startup/shutdown for RedHat

#define where jprint is - this is the directory containing directories log, bin, conf etc
JPRINT_HOME=${JPRINT_HOME:-"/usr/local/jprint"}

#make java is on your path
JAVA_HOME=${JAVA_HOME:-"/usr/local/java"}

#define the classpath for the shutdown class
JPRINTCP=${JPRINTCP:-"$JPRINT_HOME/bin/shutdown.jar"}

#define the script to use to start jprint
JPRINTSH=${JPRINTSH:-"$JPRINT_HOME/bin/run.sh"}

if [ -n "$JPRINT_CONSOLE" -a ! -d "$JPRINT_CONSOLE" ]; then
  # ensure the file exists
  touch $JPRINT_CONSOLE
fi

if [ -n "$JPRINT_CONSOLE" -a ! -f "$JPRINT_CONSOLE" ]; then
  echo "WARNING: location for saving console log invalid: $JPRINT_CONSOLE"
  echo "WARNING: ignoring it and using /dev/null"
  JPRINT_CONSOLE="/dev/null"
fi

#define what will be done with the console log
JPRINT_CONSOLE=${JPRINT_CONSOLE:-"/dev/null"}

#define the user under which jprint will run, or use RUNASIS to run as the current user
JPRINT_USER=${JPRINT_USER:-"root"}

CMD_START="cd $JPRINT_HOME/bin; $JPRINTSH" 
CMD_STOP="java -classpath $JPRINTCP org.jprint.Shutdown"

if [ "$JPRINT_USER" = "RUNASIS" ]; then
  SUBIT=""
else
  SUBIT="su - $JPRINT_USER -c "
fi

if [ -z "`echo $PATH | grep $JAVA_HOME`" ]; then
  export PATH=$PATH:$JAVA_HOME
fi

if [ ! -d "$JPRINT_HOME" ]; then
  echo JPRINT_HOME does not exist as a valid directory : $JPRINT_HOME
  exit 1
fi


echo CMD_START = $CMD_START


case "$1" in
start)
    cd $JPRINT_HOME/bin
    if [ -z "$SUBIT" ]; then
        eval $CMD_START >${JPRINT_CONSOLE} 2>&1 &
    else
        $SUBIT "$CMD_START >${JPRINT_CONSOLE} 2>&1 &" 
    fi
    ;;
stop)
    if [ -z "$SUBIT" ]; then
        $CMD_STOP
    else
        $SUBIT "$CMD_STOP"
    fi 
    ;;
restart)
    $0 stop
    $0 start
    ;;
*)
    echo "usage: $0 (start|stop|restart|help)"
esac


