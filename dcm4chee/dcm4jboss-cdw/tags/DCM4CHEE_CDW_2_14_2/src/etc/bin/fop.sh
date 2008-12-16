#! /bin/sh
#
# Shell script to run FOP

if [ -z "$JAVACMD" ] ; then 
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then 
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD=$JAVA_HOME/jre/sh/java
    else
      JAVACMD=$JAVA_HOME/bin/java
    fi
  else
    JAVACMD=java
  fi
fi
 
if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit
fi

DIRNAME=`dirname $0`
# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

if [ -n "$CLASSPATH" ] ; then
  LOCALCLASSPATH=$CLASSPATH
fi

LIB_DIR=${JBOSS_HOME}/server/default/lib
LOCALCLASSPATH=${LIB_DIR}/fop.jar
LOCALCLASSPATH=${LIB_DIR}/avalon-framework-cvs-20020806.jar:$LOCALCLASSPATH

$JAVACMD -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
         -classpath "$LOCALCLASSPATH" org.apache.fop.apps.Fop \
         -c ${JBOSS_HOME}/bin/fopcfg.xml "$@"

