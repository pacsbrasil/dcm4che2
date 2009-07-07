@echo off
rem -------------------------------------------------------------------------
rem copy needed JBOSS 5.1 components into DCM4CHEE installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set DCM4CHEE_HOME=%DIRNAME%..
set DCM4CHEE_SERV=%DCM4CHEE_HOME%\server\default

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate %DCM4CHEE_SERV%. Please check that you are in the
echo bin directory when running this script.
goto end

:found_dcm4chee
if not [%1] == [] goto found_arg1
echo "Usage: install_jboss <path-to-jboss-5.1.0.GA-installation-directory>"
goto end

:found_arg1
set JBOSS_HOME=%1
set JBOSS_SERV=%JBOSS_HOME%\server\web

if exist "%JBOSS_SERV%" goto found_jboss
echo Could not locate jboss-4.2.2.GA in %JBOSS_HOME%.
goto end

:found_jboss
set JBOSS_BIN=%JBOSS_HOME%\bin
set DCM4CHEE_BIN=%DCM4CHEE_HOME%\bin

rem Copies the startup/setup scripts excluding those which are explicitly over-ridden
xcopy /-Y "%JBOSS_BIN%\*" "%DCM4CHEE_BIN%"

md "%DCM4CHEE_HOME%\client"

xcopy /S "%JBOSS_HOME%\lib" "%DCM4CHEE_HOME%\lib\"
xcopy /S "%JBOSS_HOME%\common" "%DCM4CHEE_HOME%\common\"
xcopy /S "%JBOSS_HOME%\docs" "%DCM4CHEE_HOME%\docs\"

rem Copy things from server/web to server/default

rem Copy conf items
set JBOSS_CONF=%JBOSS_SERV%\conf
set DCM4CHEE_CONF=%DCM4CHEE_SERV%\conf
copy "%JBOSS_CONF%\bootstrap.xml" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\java.policy" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\jax-ws-catalog.xml" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\jbossts*" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\jndi.properties" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\standardjboss.xml" "%DCM4CHEE_CONF%"
copy "%JBOSS_CONF%\standardjbosscmp-jdbc.xml" "%DCM4CHEE_CONF%"

xcopy /S "%JBOSS_CONF%\props" "%DCM4CHEE_CONF%\props\"
xcopy /S "%JBOSS_CONF%\bindingservice.beans" "%DCM4CHEE_CONF%\bindingservice.beans\"
xcopy /S "%JBOSS_CONF%\bootstrap" "%DCM4CHEE_CONF%\bootstrap\"
xcopy /S "%JBOSS_CONF%\xmdesc" "%DCM4CHEE_CONF%\xmdesc\"

rem Copy lib and deployers directories
xcopy /S "%JBOSS_SERV%\lib" "%DCM4CHEE_SERV%\lib\"
xcopy /S "%JBOSS_SERV%\deployers" "%DCM4CHEE_SERV%\deployers\"

rem Copy deployment files selectively
set JBOSS_DEPLOY=%JBOSS_SERV%\deploy
set DCM4CHEE_DEPLOY=%DCM4CHEE_SERV%\deploy

xcopy /S "%JBOSS_DEPLOY%\http-invoker.sar" "%DCM4CHEE_DEPLOY%\http-invoker.sar\"
rem Don't over-write the server.xml file
xcopy /S /-Y "%JBOSS_DEPLOY%\jbossweb.deployer" "%DCM4CHEE_DEPLOY%\jbossweb.deployer\" 
xcopy /S "%JBOSS_DEPLOY%\jmx-console.war" "%DCM4CHEE_DEPLOY%\jmx-console.war\"
xcopy /S "%JBOSS_DEPLOY%\security" "%DCM4CHEE_DEPLOY%\security\"

copy "%JBOSS_DEPLOY%\ejb3-container*.xml" "%DCM4CHEE_DEPLOY%"
copy "%JBOSS_DEPLOY%\hdscanner*" "%DCM4CHEE_DEPLOY%"
copy "%JBOSS_DEPLOY%\jca-jboss*" "%DCM4CHEE_DEPLOY%"
copy "%JBOSS_DEPLOY%\jmx-invoker-service.xml" "%DCM4CHEE_DEPLOY%"
copy "%JBOSS_DEPLOY%\transaction-jboss-beans.xml" "%DCM4CHEE_DEPLOY%"

:end
if "%OS%" == "Windows_NT" endlocal
