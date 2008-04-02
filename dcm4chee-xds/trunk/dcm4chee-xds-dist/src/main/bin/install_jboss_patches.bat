@echo off
rem -------------------------------------------------------------------------
rem copy patched JBoss components into DCM4CHEE installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set XDS_HOME=%DIRNAME%..
set XDS_PATCHES=%XDS_HOME%\patches

if exist "%XDS_PATCHES%" goto found_patches
echo Could not locate %XDS_PATCHES%. 
echo Please check that you are in the
echo bin directory when running this script.
goto end

:found_patches
if not [%1] == [] goto found_arg1
echo "Usage: install_jboss_patches <path-to-dcm4chee-directory>"
goto end

:found_arg1
set DCM4CHEE_HOME=%1
set DCM4CHEE_SERV=%DCM4CHEE_HOME%\server\default

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate DCM4CHEE in %DCM4CHEE_HOME%.
goto end

:found_dcm4chee
xcopy /S "%XDS_SERV%" "%DCM4CHEE_SERV%" 

echo JBoss is now patched for use with DCM4CCHEE_XDS!

:end
if "%OS%" == "Windows_NT" endlocal
