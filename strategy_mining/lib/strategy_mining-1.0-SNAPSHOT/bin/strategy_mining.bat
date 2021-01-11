@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  strategy_mining startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and STRATEGY_MINING_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx8G"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\strategy_mining-1.0-SNAPSHOT.jar;%APP_HOME%\lib\guava-29.0-jre.jar;%APP_HOME%\lib\emd-1.0-SNAPSHOT.jar;%APP_HOME%\lib\mason-20.jar;%APP_HOME%\lib\ecj-27.jar;%APP_HOME%\lib\netlogo-6.1.1.jar;%APP_HOME%\lib\pshecj-1.jar;%APP_HOME%\lib\netlogo-6.1.1.jar;%APP_HOME%\lib\scala-parser-combinators_2.12-1.0.5.jar;%APP_HOME%\lib\parboiled_2.12-2.1.3.jar;%APP_HOME%\lib\shapeless_2.12-2.3.2.jar;%APP_HOME%\lib\macro-compat_2.12-1.1.1.jar;%APP_HOME%\lib\scala-library-2.12.8.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-2.11.1.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\jzlib-1.1.3.jar;%APP_HOME%\lib\all-0.27.jar;%APP_HOME%\lib\jfreechart-1.0.13.jar;%APP_HOME%\lib\itext-1.2.3.jar;%APP_HOME%\lib\pshecj-1.jar;%APP_HOME%\lib\equation-0.27.jar;%APP_HOME%\lib\simple-0.27.jar;%APP_HOME%\lib\denseC64-0.27.jar;%APP_HOME%\lib\dense64-0.27.jar;%APP_HOME%\lib\core-0.27.jar;%APP_HOME%\lib\jcommon-1.0.16.jar;%APP_HOME%\lib\flexmark-ext-autolink-0.20.0.jar;%APP_HOME%\lib\flexmark-ext-escaped-character-0.20.0.jar;%APP_HOME%\lib\flexmark-ext-typographic-0.20.0.jar;%APP_HOME%\lib\flexmark-formatter-0.20.0.jar;%APP_HOME%\lib\flexmark-0.20.0.jar;%APP_HOME%\lib\flexmark-util-0.20.0.jar;%APP_HOME%\lib\asm-all-5.0.4.jar;%APP_HOME%\lib\picocontainer-2.13.6.jar;%APP_HOME%\lib\log4j-1.2.16.jar;%APP_HOME%\lib\jmf-2.1.1e.jar;%APP_HOME%\lib\httpclient-4.2.jar;%APP_HOME%\lib\commons-codec-1.10.jar;%APP_HOME%\lib\jogl-all-2.3.2.jar;%APP_HOME%\lib\gluegen-rt-2.3.2.jar;%APP_HOME%\lib\httpmime-4.2.jar;%APP_HOME%\lib\json-simple-1.1.1.jar;%APP_HOME%\lib\rsyntaxtextarea-2.6.0.jar;%APP_HOME%\lib\config-1.3.1.jar;%APP_HOME%\lib\zip4j-1.3.2.jar;%APP_HOME%\lib\autolink-0.6.0.jar;%APP_HOME%\lib\httpcore-4.2.jar;%APP_HOME%\lib\commons-logging-1.1.1.jar;%APP_HOME%\lib\junit-4.10.jar;%APP_HOME%\lib\hamcrest-core-1.1.jar


@rem Execute strategy_mining
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %STRATEGY_MINING_OPTS%  -classpath "%CLASSPATH%" org.mitre.strategy_mining.StrategyMining %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable STRATEGY_MINING_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%STRATEGY_MINING_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
