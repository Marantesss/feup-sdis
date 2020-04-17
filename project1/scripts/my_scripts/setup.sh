#!bin/bash

# compilation
echo "Compiling java project..."
cd src
make

# rmi registry
echo "Starting RMI registry"
rmiregistry &
echo "RMI registry running in the background"
