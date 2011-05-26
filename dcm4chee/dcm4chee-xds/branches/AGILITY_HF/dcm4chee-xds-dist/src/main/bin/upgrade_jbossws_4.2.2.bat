@echo off
rem ----------------------------------------------------------------------------------------
rem Update JBOSS WS components of DCM4CHEE-XDS installation to JBossWS 3.0.1-native-2.0.4.GA
rem ----------------------------------------------------------------------------------------

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
echo "Usage: upgrade_jbossws <path-to-jbossws-3.0.1-native-2.0.4.GA-installation-directory>"
goto end

:found_arg1
set JBOSS_WS_HOME=%1
set JBOSS_WS_DEPLOY=%JBOSS_WS_HOME%\deploy

if exist "%JBOSS_WS_DEPLOY%" goto found_jboss_ws
echo Could not locate jbossws-3.0.1-native-2.0.4.GA in %JBOSS_WS_HOME%.
goto end

:found_jboss_ws
set JBOSS_WS_BIN=%JBOSS_WS_DEPLOY%\bin
set JBOSS_WS_LIB=%JBOSS_WS_DEPLOY%\lib
set DCM4CHEE_BIN=%DCM4CHEE_HOME%\bin
set DCM4CHEE_CLIENT=%DCM4CHEE_HOME%\client
set DCM4CHEE_LIB=%DCM4CHEE_SERV%\lib
set DCM4CHEE_JBOSSWS=%DCM4CHEE_SERV%\deploy\jbossws.sar

del "%DCM4CHEE_JBOSSWS%\jbossws-deploy.conf"
 
copy "%JBOSS_WS_BIN%\wsconsume.sh" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wsconsume.bat" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wsprovide.sh" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wsprovide.bat" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wsrunclient.sh" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wsrunclient.bat" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wstools.sh" "%DCM4CHEE_BIN%"
copy "%JBOSS_WS_BIN%\wstools.bat" "%DCM4CHEE_BIN%"

copy "%JBOSS_WS_LIB%\FastInfoset.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jaxb-api.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jaxb-impl.jar" "%DCM4CHEE_CLIENT%"
rem We need jaxb in DCM4CHEE_LIB instead of DCM4CHEE_JBOSSWS for XDS.a implementation which is not using web service stack!
copy "%JBOSS_WS_LIB%\jaxb-api.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jaxb-impl.jar" "%DCM4CHEE_LIB%"

copy "%JBOSS_WS_LIB%\jaxb-xjc.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jaxws-rt.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jaxws-tools.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jbossws-common.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jbossws-common.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jbossws-framework.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jbossws-framework.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jbossws-client.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jboss-jaxrpc.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jboss-jaxrpc.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jboss-jaxws.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jboss-jaxws.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jboss-jaxws-ext.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jboss-jaxws-ext.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jboss-saaj.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jboss-saaj.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jbossws-spi.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\jbossws-spi.jar" "%DCM4CHEE_LIB%"
copy "%JBOSS_WS_LIB%\jettison.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\policy.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\stax-api.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\stax-ex.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\streambuffer.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\wsdl4j.jar" "%DCM4CHEE_CLIENT%"
copy "%JBOSS_WS_LIB%\wstx.jar" "%DCM4CHEE_CLIENT%"

copy "%JBOSS_WS_LIB%\jboss-jaxb-intros.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\jboss-jaxrpc.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\jboss-jaxws.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\jboss-saaj.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\jbossws-core.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\policy.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\stax-api.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\wsdl4j.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\wstx.jar" "%DCM4CHEE_JBOSSWS%"
copy "%JBOSS_WS_LIB%\xmlsec.jar" "%DCM4CHEE_JBOSSWS%"

copy "%JBOSS_WS_HOME%\build\jbossws-default-deploy.conf" "%DCM4CHEE_JBOSSWS%\jbossws-deploy.conf"

copy "%JBOSS_WS_DEPLOY%\resources\jbossws-jboss42.sar\META-INF\jboss-service.xml" "%DCM4CHEE_JBOSSWS%\META-INF\jboss-service.xml"

copy "%JBOSS_WS_DEPLOY%\resources\standard-jaxws-client-config.xml" "%DCM4CHEE_JBOSSWS%\META-INF\"

:end
if "%OS%" == "Windows_NT" endlocal
