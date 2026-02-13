@echo off
setlocal

set "TARGET_DIR=C:\Users\adria\AppData\Roaming\ModrinthApp\profiles\Advanced vanilla - 1.21.4\mods"

cd /d "%~dp0"
call gradlew.bat build
if errorlevel 1 exit /b 1

set "JAR_FILE="
for /f "delims=" %%F in ('dir /b /a:-d /o:-d "build\libs\*.jar"') do (
  echo %%F | findstr /I /C:"-sources.jar" >nul && (
    rem skip
  ) || (
    echo %%F | findstr /I /C:"-dev.jar" >nul && (
      rem skip
    ) || (
      set "JAR_FILE=%%F"
      goto :copy
    )
  )
)

echo No mod jar found in build\libs
exit /b 1

:copy
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"
copy /Y "build\libs\%JAR_FILE%" "%TARGET_DIR%\" >nul
if errorlevel 1 exit /b 1

echo Copied %JAR_FILE% to %TARGET_DIR%
exit /b 0
