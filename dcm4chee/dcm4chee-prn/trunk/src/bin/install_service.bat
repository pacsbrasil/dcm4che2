@echo off
if "%1" == "uninstall" goto uninstall
if "%1" == "-uninstall" goto uninstall
if "%1" == "" goto usage
if "%2" == "" goto usage
if "%1" == "-help" goto usage
if "%1" == "-?" goto usage
if "%1" == "/?" goto usage
:install
JavaService.exe -install prnscp-1.0.0 %1\jre\bin\server\jvm.dll -Xmx128m -Djava.class.path=%1\lib\tools.jar;%2\bin\run.jar -start org.jboss.Main -stop org.jboss.Main -method systemExit -out %2\bin\out.txt -current %2\bin
goto eof
:uninstall
JavaService.exe -uninstall prnscp-1.0.0
goto eof
:usage
echo -------- To Install prnscp 1.0.0 do
echo Usage: %0 jdk_home prnscp_home
echo NOTE: You MAY NOT use spaces in the path names. If you know how
echo to fix this, please tell me.
echo Example: %0 c:\progra~1\j2sdk1.4.1_01 c:\progra~1\prnscp-1.0.0
echo --------
echo -------- To Uninstall prnscp 1.0.0 do
echo Usage: %0 uninstall
echo --------
goto eof
:eof
