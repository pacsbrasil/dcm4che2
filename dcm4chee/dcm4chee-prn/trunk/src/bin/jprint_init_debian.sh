#! /bin/sh
#
# jPrint startup/shutdown for Debian GNU/Linux
#
# Either amend this script for your requirements
# or just ensure that the following variables are set correctly 
# before calling the script

JAVA_HOME=${JAVA_HOME:-"/usr/local/java"}

if [ ! -f "$JAVA_HOME/lib/tools.jar" ]; then
  echo Could not locate JDK in JAVA_HOME : $JAVA_HOME
  exit 1
fi

JPRINT_HOME=${JPRINT_HOME:-"/usr/local/jprint"}

if [ ! -f "$JPRINT_HOME/bin/run.jar" ]; then
  echo Could not locate jPrint in JPRINT_HOME : $JPRINT_HOME
  exit 1
fi

JPRINT_USER=${JPRINT_USER:-"root"}

# PID_FILE must be writeable for JPRINT_USER!
PID_FILE=/var/run/jprint.pid

JAVA="$JAVA_HOME/bin/java"
JAVA_OPTS="-server -Xmx100m"
JAVA_CP="$JPRINT_HOME/bin/run.jar:$JAVA_HOME/lib/tools.jar"
ARGS="$JAVA_OPTS -cp $JAVA_CP org.jboss.Main"

case "$1" in
  start)
	echo -n "Starting jPrint DICOM Print Server "
        cd $JPRINT_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $JPRINT_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  stop)
	echo -n "Stopping jPrint DICOM Print Server "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	echo "."
	;;
  restart)
	echo -n "Restarting jPrint DICOM Print Server "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	sleep 10
        cd $JPRINT_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $JPRINT_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  *)
	echo "Usage: $0 {start|stop|restart}" >&2
	exit 1
	;;
esac

exit 0
