How to compile and execute this project

When running these commands do so in the project's root directory

 --- COMPILATION:

 - Linux
    sh scripts/setup.sh # compiles source code and starts rmi registry

 - Windows
    scripts/setup # compiles source code
    start rmiregistry # starts rmi registry inside source folder

 --- EXECUTION:

 -- McastSnooper (Optional)

 - Linux
    sh scripts/run_mcastsnooper.sh

 - Windows
    scripts/run_mcastsnooper

 -- Peers

Using provided script:
 - Linux
    sh scripts/run_peer.sh <protocol_version> <num_peers>

 - Windows
    scripts/run_peer <protocol_version> <num_peers>

 * protocol_version: Protocol version ("1.0" for standard protocol, "2.0" for enhanced protocol)
 * num_peers: Number of peers that are going to execute.

Manually (inside src/ folder):
java peer.Peer <protocol_version> <peerID> <peer_access_point> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>

 * protocol_version: Protocol version ("1.0" for standard protocol, "2.0" for enhanced protocol)
 * peerID: Peer's unique identifier.
 * peer_access_point: Access point for RMI object.
 * MCaddress: IP address for Multi-cast Control channel
 * MCport: Port for Multi-cast Control channel.
 * MDBaddress: IP address for Multi-cast Data Backup channel.
 * MDBport: Port for Multi-cast Data Backup channel.
 * MDRaddress: IP address for Multi-cast Data Recovery channel.
 * MDRport: port for Multi-cast Data Recovery channel.
 
NOTE THAT: IP multi-cast addresses used should be between 224.0.0.0 and 224.0.0.255, i.e the reserved IPv4 multi-cast addresses for local sub-networks.

 -- TestApp

Using provided script:
 - Linux
    sh scripts/run_test.sh
 
 - Windows
    Needs to be done manually

Manually (inside src/ folder):
java TestApp <peer_ap> <sub_protocol> [ <opnd_1> | [ <opnd_2> ] ]

 * peer_ap: Access point for RMI object. ("peer_remote_1" for peer 1, "peer_remote_n" for peer n)
 * sub_protocol: Operation that the peer must execute (BACKUP, RESTORE, DELETE, RECLAIM).
 * opnd_1: Can be either the directory of the desired file (BACKUP, RESTORE and DELETE sub-protocols). Or the maximum amount of disk space (in KBytes) the peer can use (RECLAIM sub-protocol).
 * opnd_2: Used in the BACKUP protocol to specify the desired replication degree of the backed up file.


