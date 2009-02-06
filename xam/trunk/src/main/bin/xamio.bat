@echo off
rem -------------------------------------------------------------------------
rem xamio  Launcher
rem -------------------------------------------------------------------------

rem $Id$

if not "%ECHO%" == ""  echo %ECHO%
if "%OS%" == "Windows_NT"  setlocal

set MAIN_CLASS=org.XAMIO.xam.XamIO
set MAIN_JAR=xamio-1.0-SNAPSHOT.jar
set XAMLIB_JAR=xamlib-1.0-SNAPSHOT.jar
set VIM_JAR=referenceVIM-1.0-SNAPSHOT.jar

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

rem Read all command line arguments

set ARGS=
:loop
if [%1] == [] goto end
        set ARGS=%ARGS% %1
        shift
        goto loop
:end

if not "%XAMIO_HOME%" == "" goto HAVE_XAMIO_HOME

set XAMIO_HOME=%DIRNAME%

:HAVE_XAMIO_HOME

if not "%JAVA_HOME%" == "" goto HAVE_JAVA_HOME

set JAVA=java

goto SKIP_SET_JAVA_HOME

:HAVE_JAVA_HOME

set JAVA=%JAVA_HOME%\bin\java

:SKIP_SET_JAVA_HOME

set CP=%XAMIO_HOME%\
set CP=%CP%;%XAMIO_HOME%\%MAIN_JAR%
set CP=%CP%;%XAMIO_HOME%\%XAMLIB_JAR%
set CP=%CP%;%XAMIO_HOME%\%VIM_JAR%
set CP=%CP%;%XAMIO_HOME%\commons-cli-1.1.jar

"%JAVA%" %JAVA_OPTS% -cp "%CP%" %MAIN_CLASS% %ARGS%