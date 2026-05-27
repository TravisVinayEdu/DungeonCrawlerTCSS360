@echo off
setlocal

set "MAIN_CLASS=dungeoncrawler.controller.DungeonCrawler"
set "BUILD_DIR=out\production\DungeonCrawlerTCSS360"
set "SQLITE_JAR=lib\sqlite-jdbc-3.53.1.0.jar"
set "SOURCES_FILE=out\sources.txt"

set "TARGET=%~1"
if "%TARGET%"=="" set "TARGET=run"

if /I "%TARGET%"=="compile" goto compile
if /I "%TARGET%"=="run" goto run
if /I "%TARGET%"=="clean" goto clean
if /I "%TARGET%"=="help" goto help

echo Unknown target: %TARGET%
goto help

:compile
if not exist "out" mkdir "out"
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
dir /s /b "src\*.java" > "%SOURCES_FILE%"
javac -cp "%SQLITE_JAR%" -d "%BUILD_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 exit /b 1
exit /b 0

:run
call "%~f0" compile
if errorlevel 1 exit /b 1
java --enable-native-access=ALL-UNNAMED -cp "%BUILD_DIR%;%SQLITE_JAR%" %MAIN_CLASS%
exit /b %ERRORLEVEL%

:clean
if exist "out" rd /s /q "out"
exit /b 0

:help
echo Usage: build.bat [compile^|run^|clean^|help]
echo.
echo Targets:
echo   compile  Compile Java sources into out\production\DungeonCrawlerTCSS360
echo   run      Compile and run the game
echo   clean    Remove generated build output
exit /b 1
