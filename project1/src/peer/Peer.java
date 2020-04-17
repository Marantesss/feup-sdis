package peer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import channel.Channel;
import utils.Utils;
import exceptions.BadArgumentsException;
import storage.Chunk;
import storage.Storage;
import storage.StorageFile;
import threads.ConfirmationCollectorThread;
import threads.MessageSenderManagerThread;
import threads.replies.TCPChunkThread;

public class Peer implements PeerInterface {

    private int peerID;

    private String protocolVersion;

    private ScheduledThreadPoolExecutor scheduler;

    private ConcurrentHashMap<String, Channel> channels;

    private Storage storage;

    private CountDownLatch latch;

    private String restoredID;

    /**
     * key = <fileID>_<ChunkNo> value = CHUNK messages received from other peers
     */
    private ConcurrentHashMap<String, Integer> chunkMessagesReceived;

    private int port;

    private ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            validateArguments(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            printUsage();
        }

        // args[0] -> protocol version; args[1] -> peer id
        System.out.println("--- Running Peer ---");
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]));
        System.out.println("\tProtocol Version: " + args[0]);
        System.out.println("\tPeer ID: " + args[1]);

        // initialize RMI Resgistry
        try {
            System.out.println("--- Running RMI Resgistry ---");
            peer.startRegistry(args[2]);
            System.out.println("\tRemote Object Name: " + args[2]);
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        // creating channels
        peer.setChannel("MC", new Channel(args[3], Integer.parseInt(args[4]), peer));
        peer.setChannel("MDB", new Channel(args[5], Integer.parseInt(args[6]), peer));
        peer.setChannel("MDR", new Channel(args[7], Integer.parseInt(args[8]), peer));
        // executing channels
        System.out.println("--- Running Channels ---");
        peer.executeChannels();
        System.out.println("\tMulticast Control Channel: " + "Address: " + args[3] + ", Port: " + args[4]);
        System.out.println("\tMulticast Data Backup Channel: " + "Address: " + args[5] + ", Port: " + args[6]);
        System.out.println("\tMulticast Data Recovery Channel: " + "Address: " + args[7] + ", Port: " + args[8]);

        if (peer.getProtocolVersion().equals("2.0")) {
            byte[] message = peer.generateAvailableMessage(peer.getProtocolVersion(), peer.getPeerID());
            peer.scheduler.execute(new MessageSenderManagerThread(message, "MC", peer));
        }
    }

    public static void validateArguments(String[] args) throws BadArgumentsException {
        if (args.length != 9) {
            throw new BadArgumentsException("Invalid number of arguments.");
        }
    }

    public static void printUsage() {
        System.err.println(
                "Usage: java Peer <protocol_version> <peer_id> <remote_object_name> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>");
    }

    public Peer(String protocolVersion, int peerID) {
        this.protocolVersion = protocolVersion;
        this.peerID = peerID;
        this.channels = new ConcurrentHashMap<String, Channel>();
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(200);
        this.chunkMessagesReceived = new ConcurrentHashMap<String, Integer>();
        this.port = 3000 + this.peerID;
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // creating peer storage file
        if (new File("peer_" + this.peerID).exists()) {
            this.storage = Storage.read("peer_", this);
        } else {
            this.storage = new Storage(this);
            this.storage.init();
        }
    }

    @Override
    public void backup(String path, int replication) throws RemoteException {
        StorageFile file = new StorageFile(path, replication);
        this.storage.addFile(file);

        ArrayList<Chunk> chunks = file.getChunks();

        for (Chunk chunk : chunks) {
            byte[] message = generatePutChunkMessage(chunk, this.protocolVersion, this.peerID, chunk.getFileID(),
                    chunk.getNumber(), replication);
            this.scheduler.execute(new MessageSenderManagerThread(message, "MDB", this));
            this.scheduler.schedule(new ConfirmationCollectorThread(this, message, replication), 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void restore(String path) throws RemoteException {
        File file = new File(path);

        if (!file.exists())
            return;

        this.restoredID = StorageFile.generateFileID(path);

        int nChunks = (int) Math.ceil((double) file.length() / (64 * 1000));

        this.latch = new CountDownLatch(nChunks);

        if (this.protocolVersion.equals("2.0"))
            this.scheduler.execute(new TCPChunkThread(this.restoredID, this, this.serverSocket));

        for (int chunkNo = 0; chunkNo < nChunks; chunkNo++) {
            byte[] message;

            if (this.protocolVersion.equals("2.0")) {
                String address = new String();

                try {
                    address = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                message = generateGetChunkMessage(this.protocolVersion, this.peerID, this.restoredID, chunkNo, address, this.port);
            } else
                message = generateGetChunkMessage(this.protocolVersion, this.peerID, this.restoredID, chunkNo);
            
                this.scheduler.execute(new MessageSenderManagerThread(message, "MC", this));
        }
        
        try {
			this.latch.await();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
        }
        
        path = path.substring(path.lastIndexOf(Utils.getCharSeparator()) + 1);

		this.storage.restore(restoredID, path);
    }

    public void flagReceivedChunk() {
        this.latch.countDown();
    }

    @Override
    public void delete(String path) throws RemoteException {
        File file = new File(path);

        if (!file.exists())
            return;

        String fileID = StorageFile.generateFileID(path);
        byte[] message = this.generateDeleteMessage(this.protocolVersion, this.peerID, fileID);
        this.scheduler.execute(new MessageSenderManagerThread(message, "MC", this));
    }

    @Override
    public void reclaim(int space) throws RemoteException {
        // set new capacity
        this.storage.setCapacity(space * 1000);
        // calculate necessary space to reclaim
        int toReclaim = this.storage.getUsedSpace() - space * 1000;

        if (toReclaim <=0)
            return;

        List<Chunk> sorted = new ArrayList<Chunk>(this.storage.getChunks().values());
        // remove larger chunks first
		Collections.sort(sorted, Comparator.comparing(Chunk::getSize));
		Collections.reverse(sorted);

        for (Chunk chunk : sorted) {
            this.storage.deleteChunk(chunk);

            byte[] message = this.generateRemovedMessage(this.protocolVersion, this.peerID, chunk.getFileID(), chunk.getNumber());
            this.getScheduler().execute(new MessageSenderManagerThread(message, "MC", this));

            toReclaim -= chunk.getSize();

            if (toReclaim <= 0) {
                return;
            }
        }

    }

    @Override
    public String state() throws RemoteException {
        StringBuffer buf = new StringBuffer();
        
        // get stored files information
        buf.append("--- Stored Files\n");
        ArrayList<StorageFile> files = this.storage.getFiles();
        for (StorageFile file : files) {
            buf.append(file.toString());
        }

        // get stored chunks information
        buf.append("--- Stored Chunks\n");
        buf.append(this.storage.getChunksInformation());

        // get storage information
        buf.append("\n--- Storage\n");
        buf.append(this.storage.toString());

        return buf.toString();
    }

    public ScheduledThreadPoolExecutor getScheduler() {
        return this.scheduler;
    }

    public int getPeerID() {
        return this.peerID;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public Channel getChannel(String channelKey) {
        return this.channels.get(channelKey);
    }

    public Channel setChannel(String channelKey, Channel channel) {
        return this.channels.put(channelKey, channel);
    }

    public void executeChannels() {
        for (Channel channel : this.channels.values()) {
            this.scheduler.execute(channel);
        }
    }

    public Storage getStorage() {
        return this.storage;
    }

    public void startRegistry(String remoteObjectName) throws RemoteException {
        PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(remoteObjectName, stub);
    }

    public int getChunkMessagesReceived(String fileID, int chunkNo) {
        Integer value = this.chunkMessagesReceived.get(fileID + "_" + chunkNo);            
        return value == null ? 0 : value.intValue();
    }

    public void incrementChunkMessagesReceived(String fileID, int chunkNo) {
        String key = fileID + "_" + chunkNo;
		Integer value = this.chunkMessagesReceived.get(key);
		if (value != null) {
			this.chunkMessagesReceived.replace(key, value + 1);
		} else {
            this.chunkMessagesReceived.put(key, 1);
        }
    }

    public boolean hasRequestedFile(String fileID) {
        return this.restoredID.equals(fileID);
    }

    public String getRestoredID() {
        return this.restoredID;
    }

    /**
     * CHUNK BACKUP PROTOCOL
     */
    public byte[] generatePutChunkMessage(Chunk chunk, String version, int senderID, String fileID, int chunkNo, int replicationDegree) {
        byte[] body = chunk.getContent();
        String headerString = version + " PUTCHUNK " + senderID + " " + fileID + " " + chunkNo + " " + replicationDegree + " " + Utils.CRLF + Utils.CRLF;
        byte[] header = headerString.getBytes(StandardCharsets.US_ASCII);
        return Utils.concatenateArrays(header, body);
    }

    public byte[] generateStoredMessage(String version, int senderID, String fileID, int chunkNo) {
        String headerString = version + " STORED " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
        return headerString.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * CHUNK RESTORE PROTOCOL
     */
    public byte[] generateGetChunkMessage(String version, int senderID, String fileID, int chunkNo) {
        String headerString = version + " GETCHUNK " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
        return headerString.getBytes(StandardCharsets.US_ASCII);
    }

    public byte[] generateGetChunkMessage(String version, int senderID, String fileID, int chunkNo, String address, int port) {
        String headerString = version + " GETCHUNK " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + address + " " + port + " " + Utils.CRLF + Utils.CRLF;
        return headerString.getBytes(StandardCharsets.US_ASCII);
    }

    public byte[] generateChunkMessage(Chunk chunk, String version, int senderID, String fileID, int chunkNo) {
        byte[] body = chunk.getContent();
        String headerString = version + " CHUNK " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
        byte[] header = headerString.getBytes(StandardCharsets.US_ASCII);
        return Utils.concatenateArrays(header, body);
    }

    /**
     * CHUNK DELETION PROTOCOL
     */
    public byte[] generateDeleteMessage(String version, int senderID, String fileID) {
		String message = version + " DELETE " + senderID + " " + fileID + " " + Utils.CRLF + Utils.CRLF;
		return message.getBytes(StandardCharsets.US_ASCII);
    }

    public byte[] generateDeleteAckMessage(String version, int senderID, int initiatorID, String fileID) {
		String message = version + " DELETEACK " + senderID + " " + initiatorID + " " + fileID + " " + Utils.CRLF + Utils.CRLF;
		return message.getBytes(StandardCharsets.US_ASCII);
    }

    public byte[] generateAvailableMessage(String version, int senderID) {
		String message = version + " AVAILABLE " + senderID + " " + Utils.CRLF + Utils.CRLF;
		return message.getBytes(StandardCharsets.US_ASCII);
    }
    
    /**
     * SPACE RECLAMING PROTOCOL
     */
    public byte[] generateRemovedMessage(String version, int senderID, String fileID, int chunkNo) {
		String message = version + " REMOVED " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
		return message.getBytes(StandardCharsets.US_ASCII);
    }
}
