@echo off

cd src
REM backup and wait
java TestApp peer_remote_1 BACKUP ../tests/clean_code.pdf 2
timeout /t 2 /nobreak > NUL

cd ..
REM restore and wait
java TestApp peer_remote_1 RESTORE ../tests/clean_code.pdf
timeout /t 2 /nobreak > NUL

REM delete and wait
java TestApp peer_remote_1 DELETE ../tests/clean_code.pdf
timeout /t 2 /nobreak > NUL

REM reclaim and wait
java TestApp peer_remote_2 RECLAIM 100
timeout /t 2 /nobreak > NUL

REM state and wait
java TestApp peer_remote_1 STATE
timeout /t 2 /nobreak > NUL
