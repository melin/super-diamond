@REM ----------------------------------------------------------------------------
@REM super-diamond-server
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM author: yuwang@iflytek.com
@REM date: 2017/09/12
@REM ----------------------------------------------------------------------------

@echo off

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:init
cd %~dp0
set SCRIPT_DIR=%CD%
set BASE_HOME=%SCRIPT_DIR%

:findBaseDir
IF EXIST "%BASE_HOME%\bin\server_run.cmd" goto baseDirFound
cd ..
IF "%SCRIPT_DIR%"=="%CD%" goto baseDirNotFound
set BASE_HOME=%CD%
goto findBaseDir

:baseDirFound
cd "%BASE_HOME%"
goto endDetectBaseDir

:baseDirNotFound
echo 'Wrong directory structure... >&2'
goto error

:endDetectBaseDir

:setClassPath
set CLASSPATH=%BASE_HOME%\conf;%BASE_HOME%\lib\*

:run

cd %BASE_HOME%

set SERVER_NAME=super-diamond-server
set HOST_NAME=%COMPUTERNAME%
set JMX_PORT=4001
set BASE_JVM_ARGS=-Xmx512m -Xms256m -server
set APP_JVM_ARGS=%BASE_JVM_ARGS% -cp %CLASSPATH%
@REM default profile is development
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set BASE_APP_ARGS=


@echo on
%JAVA_EXE% %APP_JVM_ARGS% -DBASE_HOME="%BASE_HOME%" -DSERVER_NAME=%SERVER_NAME%-%HOST_NAME% -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false  -Dcom.sun.management.jmxremote.port=%JMX_PORT% %BASE_APP_ARGS% com.github.diamond.jetty.JettyServer
@echo off

goto end

:error
set ERROR_CODE=1

exit /B %ERROR_CODE%

:end
exit /B 0

