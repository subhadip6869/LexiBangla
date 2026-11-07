@echo off
setlocal enabledelayedexpansion

echo ==============================================================================
echo  LexiBangla Desktop - Windows EXE Installer Packaging (Optimized Bundled JRE)
echo ==============================================================================

echo === [1/3] Building Shaded Application JAR with Maven ===
call mvn clean package -DskipTests --batch-mode

for %%F in (target\lexibangla-*-all.jar) do (
    set JAR_FILENAME=%%~nxF
)

if "%JAR_FILENAME%"=="" (
    echo [ERROR] Fat JAR not found in target!
    exit /b 1
)

echo === [2/3] Generating Minimal Bundled JRE with jlink ===
if exist target\custom-runtime rmdir /s /q target\custom-runtime

set MODULES=java.base,java.compiler,java.desktop,java.instrument,java.naming,java.net.http,java.rmi,java.scripting,java.sql,java.xml,jdk.crypto.ec,jdk.localedata,jdk.unsupported

jlink --no-header-files --no-man-pages --strip-debug --compress=2 --add-modules %MODULES% --output target\custom-runtime

if not exist dist mkdir dist

echo === [3/3] Generating Native Windows EXE Installer with jpackage ===
REM Stage only the shaded JAR into a dedicated input folder to prevent target files & custom-runtime leaking into /app
if exist target\package-input rmdir /s /q target\package-input
mkdir target\package-input
copy "target\%JAR_FILENAME%" "target\package-input\" >nul

jpackage ^
  --type exe ^
  --dest dist\ ^
  --name "LexiBangla" ^
  --app-version "1.0.1" ^
  --vendor "OriginSD" ^
  --copyright "© 2026 OriginSD" ^
  --description "English-to-Bengali Offline Dictionary" ^
  --icon "src\main\resources\lexibangla-logo.ico" ^
  --input target\package-input ^
  --main-jar "%JAR_FILENAME%" ^
  --main-class "org.originsd.lexibangla.Launcher" ^
  --runtime-image target\custom-runtime ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser

echo === Native Windows EXE Installer Created in dist\ ===
dir dist\*.exe
