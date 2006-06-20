BUILD INSTRUCTIONS FOR DCM4CHEE ARCHIVE:
========================================

In order to build all components of the dcm4chee archive application you have
to download and install:

- jai_imageio-1_0_01
  (from http://java.sun.com/products/java-media/jai/downloads/download-iio-1_0_01.html)

- Ant 1.6.x (from http://ant.apache.org)

- JUnit 3.8.x (from http://junit.org)

- Jakarta Cactus 1.7 for J2EE 1.3 [http://jakarta.apache.org/cactus/]
(needed to build dcm4jboss-ejb, which itself required to build dcm4chee-sar)

- XDoclet 1.2.3 [http://xdoclet.sourceforge.net]

- JBoss 4.0.3SP1 [http://jboss.org]

- Maverick 2.2.3 + Domify 2.0.3 [http://mav.sourceforge.net]

- Apache FOP 0.20.5 [http://xmlgraphics.apache.org/fop/]

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

Copy junit.jar from the JUnit package into $ANT_HOME/lib/.

You need to build the dcm4che core library and the dcm4chee-ejb 
components from the source, before you can build dcm4chee-web.

At first time, connecting to the project CVS repository, you have to login by

>cvs -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che login
CVS password:<enter>

== 1. Building the dcm4che core library ==

Download the source of the dcm4che core library by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4che14 

and build the library jar, by running

>ant jar

with the dcm4che14 distribution directory as working directory.

== 2. Building the Audit Record Repository application ==

Download the source of the dcm4chee-arr package by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4chee-arr

The default build script (build.xml) assumes following locations of
components required for building dcm4chee-arr:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your XDoclet bundle dist location -->
<property name="xdoclet.home" value="${user.home}/xdoclet-1.2.3"/>

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.3SP1"/>	

You may also disable the build of versions for Oracle9i/10g and/or DB2 V8.1.
Otherwise you may have to install a patch utility, neeed for that builds.

<!-- Uncomment to skip patch task (=not available on Windows) -->
<!--property name="oracle.nobuild" value="true" -->
<!--property name="db2.nobuild" value="true" -->
	
Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant all

with the dcm4chee-arr distribution directory as working directory
builds database specific ejb-jars and jars of the Audit Record Repository
application.

== 3. Building the EJB and the JDBC Data Access components ==

Download the source of the dcm4chee-ejb package by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4jboss-ejb

The default build script (build.xml) assumes following locations of
components required for building dcm4jboss-ejb:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your XDoclet bundle dist location -->
<property name="xdoclet.home" value="${user.home}/xdoclet-1.2.3"/>

<!-- Override with your Cactus bundle dist location -->
<property name="cactus.home" value="${user.home}/jakarta-cactus-13-1.7"/>

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.2"/>	

You may also disable the build of versions for Oracle9i/10g and/or DB2 V8.1.
Otherwise you may have to install a patch utility, neeed for that builds.

<!-- Uncomment to skip patch task (=not available on Windows) -->
<!--property name="oracle.nobuild" value="true" -->
<!--property name="db2.nobuild" value="true" -->
	
Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant jar

with the dcm4jboss-ejb distribution directory as working directory
builds database specific ejb-jars and jars needed for EJB clients and
for direct DB access via JDBC.

== 4. Building DICOM services components ==

The source of the dcm4chee-sar package is available in module dcm4chee-sar
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4chee-sar

The default build script (build.xml) assumes following locations of
components required for building dcm4chee-sar:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your dcm4jboss-ejb dist location -->
<property name="dcm4jboss-ejb.home" value="../dcm4jboss-ejb"/>		

<!-- Override with your dcm4chee-arr dist location -->
<property name="dcm4chee-arr.home" value="../dcm4chee-arr"/>	

<!-- Override with your dcm4chee-hl7 dist location -->
<property name="dcm4chee-hl7.home" value="../dcm4chee-hl7"/>		

<!-- Override with your dcm4chee-wado dist location -->
<property name="dcm4chee-wado.home" value="../dcm4chee-wado"/>		
	
<!-- Override with your FOP dist location -->
<property name="fop.home" value="${user.home}/fop-0.20.5"/>
    
<!-- Override with your XDoclet bundle dist location -->
<property name="xdoclet.home" value="${user.home}/xdoclet-1.2.3"/>

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.3SP1"/>		

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.


After that, invoking

>ant jar

with the dcm4chee-sar distribution directory as working directory generates

target/lib/dcm4chee.jar
.

== 5. Building HL7 services components ==

The source of the dcm4chee-hl7 package is available in module dcm4chee-hl7
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4chee-hl7

The default build script (build.xml) assumes following locations of
components required for building dcm4chee-hl7:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your dcm4jboss-ejb dist location -->
<property name="dcm4jboss-ejb.home" value="../dcm4jboss-ejb"/>		

<!-- Override with your dcm4chee-sar dist location -->
<property name="dcm4chee-sar.home" value="../dcm4chee-sar"/>		

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.3SP1"/>		

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.


After that, invoking

>ant jar

with the dcm4chee-hl7 distribution directory as working directory generates

target/lib/dcm4chee-hl7.jar
.

== 6. Building the WADO and IHE RID Services ==

The source of the dcm4chee-wado package is available in module dcm4chee-wado
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4chee-wado

The default build script (build.xml) assumes following locations of
components required for building dcm4chee-wado:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your dcm4jboss-ejb dist location -->
<property name="dcm4jboss-ejb.home" value="../dcm4jboss-ejb"/>		

<!-- Override with your dcm4chee-sar dist location -->
<property name="dcm4chee-sar.home" value="../dcm4chee-sar"/>		

<!-- Override with your FOP dist location -->
<property name="fop.home" value="${user.home}/fop-0.20.5"/>
    
<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.3SP1"/>		

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.


After that, invoking

>ant jar war

with the dcm4chee-wado distribution directory as working directory generates

target/lib/dcm4chee-wado.jar
target/deploy/dcm4chee-wado.war
.

== 7. Building the Web Application Archive ==

The source of the dcm4chee-web package is available in module dcm4chee-web
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4chee-web

The default build script (build.xml) assumes following locations of
components required for building dcm4chee-web:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your dcm4jboss-ejb dist location -->
<property name="dcm4jboss-ejb.home" value="../dcm4jboss-ejb"/>		

<!-- Override with your dcm4chee-sar dist location -->
<property name="dcm4chee-sar.home" value="../dcm4chee-sar"/>		

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-4.0.3SP1" />

<!-- Override with your Maverick dist location -->
<property name="maverick.home" value="../maverick" />

<!-- Override with your Domify dist location -->
<property name="opt-domify.home" value="../opt-domify" />

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant war

with the dcm4chee-war dist directory as working directory
generates the Web Application Archive

target/dcm4chee-web.war
.


== 8. Building the Binary Distribution package ==

After changing the working directory back to the dcm4chee-sar distribution,
invoke

>ant dist

to generates the distribution packages

target/dcm4chee-<version>.zip
target/dcm4chee-standalone-hsql-<version>.zip
target/dcm4chee-standalone-psql-<version>.zip
target/dcm4chee-standalone-mysql-<version>.zip
target/dcm4chee-standalone-db2-<version>.zip
target/dcm4chee-standalone-oracle-<version>.zip
.
