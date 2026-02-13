@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "DEFAULT_DEST_DIR=C:\Users\adria\AppData\Roaming\ModrinthApp\profiles\Advanced vanilla - 1.21.4\mods"

if not "%~2"=="" (
  echo Usage: %~nx0 [destination_folder]
  echo If omitted, defaults to:
  echo   %DEFAULT_DEST_DIR%
  exit /b 1
)

if "%~1"=="" (
  set "DEST_DIR=%DEFAULT_DEST_DIR%"
) else (
  set "DEST_DIR=%~1"
)
set "SCRIPT_DIR=%~dp0"

if not exist "%DEST_DIR%" mkdir "%DEST_DIR%"

echo Building mod...
pushd "%SCRIPT_DIR%"
call ./gradlew build
if errorlevel 1 (
  popd
  echo Build failed.
  exit /b 1
)

set "JAR_FILE="
for /f "delims=" %%F in ('dir /b /a:-d /o:-d "build\libs\*.jar"') do (
  echo %%F | findstr /I /C:"-sources.jar" >nul && (
    rem skip
  ) || (
    echo %%F | findstr /I /C:"-dev.jar" >nul && (
      rem skip
    ) || (
      echo %%F | findstr /I /C:"-javadoc.jar" >nul && (
        rem skip
      ) || (
        set "JAR_FILE=%%F"
        goto :found
      )
    )
  )
)

:found
if "%JAR_FILE%"=="" (
  popd
  echo Error: no built mod JAR found in build\libs
  exit /b 1
)

copy /Y "build\libs\%JAR_FILE%" "%DEST_DIR%\" >nul
if errorlevel 1 (
  popd
  echo Copy failed.
  exit /b 1
)

popd
echo Copied: %JAR_FILE%
echo To: %DEST_DIR%
exit /b 0
