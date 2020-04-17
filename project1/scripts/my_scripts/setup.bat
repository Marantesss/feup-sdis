@echo off

REM compilation
cd src
make

REM rmi registry (not working)
REM start cmd.exe /k "start rmiregistry"