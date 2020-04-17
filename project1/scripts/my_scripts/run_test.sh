#!/bin/bash

cd src

# backup and wait
java TestApp peer_remote_1 BACKUP ../tests/clean_code.pdf 2
sleep 2

# restore and wait
java TestApp peer_remote_1 RESTORE ../tests/clean_code.pdf
sleep 2

# delete and wait
java TestApp peer_remote_1 DELETE ../tests/clean_code.pdf
sleep 2

# reclaim and wait
java TestApp peer_remote_2 RECLAIM 100
sleep 2

# state and wait
java TestApp peer_remote_1 STATE
sleep 2
