BUILD INSTRUCTIONS FOR DCM4CHE:
===============================

In order to build dcm4che you have to download
and install:
- Ant 1.5.x (from http://jakarta.apache.org/ant)

Create an environment variable pointing to
the Ant home directory (ANT_HOME) or create a
shell script calling "$ANT_HOME/bin/ant" or
"%ANT_HOME%\bin\ant" and start the Ant script with:
   $ANT_HOME/bin/ant release
or the script - with this directory as working directory.

To run the JUnit tests:

$ANT_HOME/bin/ant test

, you have to copy lib/junit.jar to $ANT_HOME/lib,
and you may also need to download the test data before,
which are only available directly from the 
project CVS at sourceforge:

At first time, connecting to the project CVS, you have to login by

cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  login

password:<enter>

, before you can checkout the testdata:

cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4che14/test/conf/data


