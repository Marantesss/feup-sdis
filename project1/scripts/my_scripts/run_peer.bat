@echo off

set version=%1
set numPeers=%2
set start=%3

REM 224.0.0.0 to 224.0.0.255 -> IPv4 Local Subnetwork for multicasting

cd src
for /l %%x in (%start%, 1, %numPeers%) do (
    start cmd.exe /k "java peer.Peer %version% %%x peer_remote_%%x 224.0.0.1 8001 224.0.0.2 8081 224.0.0.3 8082"
)
pause