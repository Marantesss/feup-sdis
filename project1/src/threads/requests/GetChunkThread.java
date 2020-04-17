package threads.requests;

import peer.Peer;
import storage.Chunk;
import threads.MessageSenderManagerThread;
import utils.Utils;

public class GetChunkThread implements Runnable {

    private String version;

    private int senderID;

    private String fileID;

    private int chunkNo;

    private Peer peer;

    private String address;

    private int port;

    public GetChunkThread(byte[] message, Peer peer) {
        // <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.version = header[0];
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = Integer.parseInt(header[4]);

        if (this.version.equals("2.0")) {
            this.address = header[5].trim();
            this.port = Integer.parseInt(header[6]);
        }

        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received GETCHUNK message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tChunk Number: " + this.chunkNo + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        // peer should not answer to their own GETCHUNK message
        if (this.peer.getPeerID() == this.senderID) {
            return;
        }

        // check if peer contains the requested chunk
        if (!this.peer.getStorage().contains(this.fileID, this.chunkNo)) {
            System.out.println(this.getInfo("Requested chunk not available"));
            return;
        }
        
        // check if requested chunk was already sent by another peer
        // get current number of received CHUNK messages for that pier
        int lastReceivedMessages = this.peer.getChunkMessagesReceived(this.fileID, this.chunkNo);
        try {
            // wait a random amount of time between 0 and 400 ms
            int waitingTime = Utils.getRandomNumber(0, 400);
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // get current number of received CHUNK messages for that pier
        int thisReceivedMessages = this.peer.getChunkMessagesReceived(this.fileID, this.chunkNo);
        // if number of received messages differ, then another peer already replied
        if (thisReceivedMessages != lastReceivedMessages) {
            System.out.println(this.getInfo("Requested chunk already sent by another peer"));
            return;
        }

        // get requested chunk
        Chunk requestedChunk = this.peer.getStorage().getChunk(this.fileID, this.chunkNo);
        // Reply with CHUNK
        byte[] reply = this.peer.generateChunkMessage(requestedChunk, this.peer.getProtocolVersion(), this.peer.getPeerID(), requestedChunk.getFileID(), requestedChunk.getNumber());
        
        if (this.version.equals("1.0"))
            this.peer.getScheduler().execute(new MessageSenderManagerThread(reply, "MDR", this.peer));
        else
            this.peer.getScheduler().execute(new TCPGetChunkThread(reply, this.address, this.port));
        
        System.out.println(this.getInfo("Requested chunk sent successfully"));
    }

}