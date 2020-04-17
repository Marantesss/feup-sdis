#!/bin/bash

version=$1
num_peers=$2

if [ $# -ne 2 ]; then
    echo "Usage: $0 <version> <number of peers>"
    exit 1
fi

# 224.0.0.0 to 224.0.0.255 -> IPv4 Local Subnetwork for multicasting

cd src
for ((i = 1 ; i <= num_peers ; i++)); do
    # konsole -x sh -c "java peer.Peer $version $i peer_remote 224.0.0.1 8001 224.0.0.2 8002 224.0.0.3 8003"
    gnome-terminal -x sh -c "java peer.Peer $version $i peer_remote_$i 224.0.0.1 8001 224.0.0.2 8002 224.0.0.3 8003"
done