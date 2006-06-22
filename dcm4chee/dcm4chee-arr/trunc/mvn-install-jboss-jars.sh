#!/bin/sh
JBOSS_LIB=$JBOSS_HOME/server/default/lib
JBOSS_EJB3=$JBOSS_HOME/server/default/deploy/ejb3.deployer
mvn install:install-file -Dfile=$JBOSS_LIB/jboss-j2ee.jar -DgroupId=jboss \
    -DartifactId=jboss-j2ee -Dversion=4.0.4.GA -Dpackaging=jar
mvn install:install-file -Dfile=$JBOSS_LIB/ejb3-persistence.jar -DgroupId=jboss \
    -DartifactId=ejb3-persistence -Dversion=4.0.4.GA -Dpackaging=jar
mvn install:install-file -Dfile=$JBOSS_EJB3/jboss-ejb3.jar -DgroupId=jboss \
    -DartifactId=jboss-ejb3 -Dversion=4.0.4.GA -Dpackaging=jar
mvn install:install-file -Dfile=$JBOSS_EJB3/jboss-ejb3x.jar -DgroupId=jboss \
    -DartifactId=jboss-ejb3x -Dversion=4.0.4.GA -Dpackaging=jar
mvn install:install-file -Dfile=$JBOSS_EJB3/jboss-annotations-ejb3.jar -DgroupId=jboss \
    -DartifactId=jboss-annotations-ejb3 -Dversion=4.0.4.GA -Dpackaging=jar
