setlocal 
cd \src
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\jboss-seam.jar -DartifactId=jboss-seam -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\jboss-seam-ui.jar -DartifactId=jboss-seam-ui -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\jboss-seam-debug.jar -DartifactId=jboss-seam-debug -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\hibernate-all.jar -DartifactId=hibernate-all -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\jboss-ejb3-all.jar -DartifactId=jboss-ejb3-all -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\thirdparty-all.jar -DartifactId=thirdparty-all -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
rem call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\.jar -DartifactId= -DgroupId=org.jboss.seam -Dversion=1.1.6.GA -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\jsf-facelets.jar -DartifactId=jsf-facelets -DgroupId=com.sun.facelets -Dversion=1.1.62 -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\el-api.jar -DartifactId=el-api -DgroupId=javax.el -Dversion=1.2 -DgeneratePom=true -Dpackaging=jar
call mvn install:install-file -Dfile=jboss-seam-1.1.6.GA\lib\el-ri.jar -DartifactId=el-ri -DgroupId=javax.el -Dversion=1.2 -DgeneratePom=true -Dpackaging=jar
