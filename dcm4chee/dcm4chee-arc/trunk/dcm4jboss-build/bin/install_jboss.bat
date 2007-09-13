@echo off
rem -------------------------------------------------------------------------
rem xcopy needed JBOSS components into DCM4CHEE installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set DCM4CHEE_HOME="%DIRNAME%"\..
set DCM4CHEE_SERV="%DCM4CHEE_HOME%"\server\default

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate %DCM4CHEE_SERV%. Please check that you are in the
echo bin directory when running this script.
goto end

:found_dcm4chee
if not [%1] == [] goto found_arg1
echo "Usage: install_jboss <path-to-jboss-4.2.1.GA-installation-directory>"
goto end

:found_arg1
set JBOSS_HOME="%1"
set JBOSS_SERV="%JBOSS_HOME%"\server\default

if exist "%JBOSS_HOME%\bin\run.jar" goto found_jboss
echo Could not locate jboss-4.2.1.GA in %JBOSS_HOME%.
goto end

:found_jboss
xcopy "%JBOSS_HOME%"\bin\run.bat \
  "%JBOSS_HOME%"\bin\run.jar \
  "%JBOSS_HOME%"\bin\run.sh \
  "%JBOSS_HOME%"\bin\shutdown.bat \
  "%JBOSS_HOME%"\bin\shutdown.jar \
  "%JBOSS_HOME%"\bin\shutdown.sh \
  "%JBOSS_HOME%"\bin\twiddle.bat \
  "%JBOSS_HOME%"\bin\twiddle.jar \
  "%JBOSS_HOME%"\bin\twiddle.sh \
  "%DCM4CHEE_HOME%"\bin

md "%DCM4CHEE_HOME%"\client
xcopy "%JBOSS_HOME%"\client\jbossall-client.jar "%DCM4CHEE_HOME%"\client

xcopy /S "%JBOSS_HOME%"\lib "%DCM4CHEE_HOME%"

xcopy "%JBOSS_SERV%"\conf\jbossjta-properties.xml \
  "%JBOSS_SERV%"\conf\jboss-service.xml \
  "%JBOSS_SERV%"\conf\jndi.properties \
  "%DCM4CHEE_SERV%"\conf
xcopy /S "%JBOSS_SERV%"\conf\props \
  "%JBOSS_SERV%"\conf\xmdesc \
  "%DCM4CHEE_SERV%"\conf

xcopy "%JBOSS_SERV%"\lib\* "%DCM4CHEE_SERV%"\lib

xcopy "%JBOSS_SERV%"\deploy\bsh-deployer.xml \
  "%JBOSS_SERV%"\deploy\cache-invalidation-service.xml \
  "%JBOSS_SERV%"\deploy\client-deployer-service.xml \
  "%JBOSS_SERV%"\deploy\ear-deployer.xml \
  "%JBOSS_SERV%"\deploy\ejb3-interceptors-aop.xml \
  "%JBOSS_SERV%"\deploy\jboss-ha-local-jdbc.rar \
  "%JBOSS_SERV%"\deploy\jboss-ha-xa-jdbc.rar \
  "%JBOSS_SERV%"\deploy\jbossjca-service.xml \
  "%JBOSS_SERV%"\deploy\jboss-local-jdbc.rar \
  "%JBOSS_SERV%"\deploy\jboss-xa-jdbc.rar \
  "%JBOSS_SERV%"\deploy\jmx-invoker-service.xml \
  "%JBOSS_SERV%"\deploy\jsr88-service.xml \
  "%JBOSS_SERV%"\deploy\mail-service.xml \
  "%JBOSS_SERV%"\deploy\monitoring-service.xml \
  "%JBOSS_SERV%"\deploy\properties-service.xml \
  "%JBOSS_SERV%"\deploy\quartz-ra.rar \
  "%JBOSS_SERV%"\deploy\sqlexception-service.xml \
  "%DCM4CHEE_SERV%"\deploy

xcopy /S "%JBOSS_SERV%"\deploy\ejb3.deployer \
  "%JBOSS_SERV%"\deploy\http-invoker.sar \
  "%JBOSS_SERV%"\deploy\jboss-aop-jdk50.deployer \
  "%JBOSS_SERV%"\deploy\jboss-bean.deployer \
  "%JBOSS_SERV%"\deploy\jbossws.sar \
  "%DCM4CHEE_SERV%"\deploy

xcopy "%JBOSS_SERV%"\deploy\jboss-web.deployer\context.xml \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jasper-jdt.jar \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jbossweb-extras.jar \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jbossweb.jar \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jbossweb-service.jar \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jstl.jar \
  "%DCM4CHEE_SERV%"\deploy\jboss-web.deployer
  
xcopy /S "%JBOSS_SERV%"\deploy\jboss-web.deployer\conf \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\jsf-libs \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\META-INF \
  "%JBOSS_SERV%"\deploy\jboss-web.deployer\ROOT.war \
  "%DCM4CHEE_SERV%"\deploy\jboss-web.deployer

xcopy "%JBOSS_SERV%"\deploy\jms\jms-ds.xml \
  "%JBOSS_SERV%"\deploy\jms\jms-ra.rar \
  "%JBOSS_SERV%"\deploy\jms\jvm-il-service.xml \
  "%JBOSS_SERV%"\deploy\jms\uil2-service.xml \
  "%DCM4CHEE_SERV%"\deploy\jms

xcopy "%JBOSS_SERV%"\deploy\jmx-console.war\checkJNDI.jsp \
  "%JBOSS_SERV%"\deploy\jmx-console.war\displayMBeans.jsp \
  "%JBOSS_SERV%"\deploy\jmx-console.war\displayOpResult.jsp \
  "%JBOSS_SERV%"\deploy\jmx-console.war\index.jsp \
  "%JBOSS_SERV%"\deploy\jmx-console.war\jboss.css \
  "%JBOSS_SERV%"\deploy\jmx-console.war\style_master.css \
  "%DCM4CHEE_SERV%"\deploy\jmx-console.war
  
xcopy /S "%JBOSS_SERV%"\deploy\jmx-console.war\cluster \
  "%JBOSS_SERV%"\deploy\jmx-console.war\images \
  "%JBOSS_SERV%"\deploy\jmx-console.war\META-INF \
  "%DCM4CHEE_SERV%"\deploy\jmx-console.war
  
xcopy /S "%JBOSS_SERV%"\deploy\jmx-console.war\WEB-INF\classes \
  "%DCM4CHEE_SERV%"\deploy\jmx-console.war\WEB-INF

xcopy "%JBOSS_SERV%"\deploy\management\console-mgr.sar\*.jar \
  "%DCM4CHEE_SERV%"\deploy\management\console-mgr.sar

xcopy /S "%JBOSS_SERV%"\deploy\management\console-mgr.sar\META-INF \
  "%DCM4CHEE_SERV%"\deploy\management\console-mgr.sar

xcopy "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\*.html \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\*.jar \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\*.js \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\*.jsp \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\*.xml \
  "%DCM4CHEE_SERV%"\deploy\management\console-mgr.sar\web-console.war
  
xcopy /S "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\css \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\images \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\img \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\META-INF \
  "%DCM4CHEE_SERV%"\deploy\management\console-mgr.sar\web-console.war
  
xcopy /S "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\WEB-INF\classes \
  "%JBOSS_SERV%"\deploy\management\console-mgr.sar\web-console.war\WEB-INF\tlds \
  "%DCM4CHEE_SERV%"\deploy\management\console-mgr.sar\web-console.war\WEB-INF

:end
