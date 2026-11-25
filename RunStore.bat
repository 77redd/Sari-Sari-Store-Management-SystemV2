@echo off
rem Launches the Sari-Sari Store system. Records are saved in a data\ folder next to this file.
title Sari-Sari Store Management System
cd /d "%~dp0"

if exist "SariSariStore.exe" (
    rem Inside the released zip: run the bundled exe (no Java install needed)
    ".\SariSariStore.exe"
) else if exist "SariSariStore\SariSariStore.exe" (
    rem Repo root after a local jpackage build
    ".\SariSariStore\SariSariStore.exe"
) else (
    rem Source checkout: compile and run with an installed JDK
    javac -d bin src\Main.java src\models\*.java src\managers\*.java src\util\*.java || goto end
    java -cp bin Main
)

:end
pause
