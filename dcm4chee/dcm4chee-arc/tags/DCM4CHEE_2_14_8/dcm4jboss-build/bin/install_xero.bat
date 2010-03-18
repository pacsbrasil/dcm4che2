@echo off
rem -----------------------------------------------------------------------------------
rem copy XERO and WADO2 installation from install directory into a running DCM4CHEE System
rem -----------------------------------------------------------------------------------

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
echo "Usage: install_xero <path-to-dcm4chee-xero-installation-directory>"
goto end

:found_arg1
set XER_HOME=%1
set XER_SERV=%XER_HOME%\server\default

if exist "%XER_SERV%\deploy\xero.war" set XER_WADO=xero
if not [%XER_WADO%] == [] goto found_xer
echo Could not locate xero.war in %XER_HOME%.
goto end

:found_xer
copy "%XER_SERV%\conf\ae-local.properties" "%DCM4CHEE_SERV%\conf"

copy "%XER_SERV%\deploy\wado2.war" "%DCM4CHEE_SERV%\deploy"
copy "%XER_SERV%\deploy\xero.war" "%DCM4CHEE_SERV%\deploy"
copy "%XER_SERV%\deploy\jboss-web.deployer\server.xml" "%DCM4CHEE_SERV%\deploy\jboss-web.deployer" /Y

:end
if "%OS%" == "Windows_NT" endlocal
