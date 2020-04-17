package threads.requests;

import peer.Peer;
import storage.Chunk;
import threads.MessageSenderManagerThread;
import utils.Utils;

public class PutChunkThread implements Runnable {

    private int senderID;

    private String fileID;

    private int chunkNo;

    private int replicationDegree;

    private byte[] chunkContent;

    private Peer peer;

    public PutChunkThread(byte[] message, Peer peer) {
        // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = Integer.parseInt(header[4]);
        this.replicationDegree = Integer.parseInt(header[5]);

        this.chunkContent = Utils.getBody(message);
        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received PUTCHUNK message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tChunk Number: " + this.chunkNo + "\n");
        buf.append("\tReplication Degree: " + this.replicationDegree + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        // A peer ignores its own messages
		if (this.peer.getPeerID() == this.senderID) {
            return;
        }
        // If peer's storage contains this chunk, then do nothing
        if (this.peer.getStorage().contains(this.fileID, this.chunkNo)) {
            System.out.println(this.getInfo("ALDREADY STORED"));
            return;
        }
        // If no more storage space is availabe, then do nothing
        if (!this.peer.getStorage().hasAvailableSpace(this.chunkContent.length)) {
            System.out.println(this.getInfo("NO SPACE AVAILABLE FOR CHUNK"));
            return;
        }
        // TODO In Enchancement
        // if the current replication degree of the chunk is already the desired one, then do nothing
        /*
        if(this.peer.getStorage().getNConfirmationMessages(this.fileID, this.chunkNo) >= this.replicationDegree) {
            System.out.println("Replication degree already satisfied");
            return;
        }
        */

        /* TODO:
            - A peer has to keep stats of what it does
            - number of Stored messages sent
        */
        // store chunk
        this.peer.getStorage().addChunk(new Chunk(this.fileID, this.chunkNo, this.chunkContent, this.chunkContent.length, this.replicationDegree));
        System.out.println(this.getInfo("Chunk stored successfully\n"));
        // Reply with STORED
        byte[] reply = this.peer.generateStoredMessage(this.peer.getProtocolVersion(), this.peer.getPeerID(), this.fileID, this.chunkNo);
        this.peer.getScheduler().execute(new MessageSenderManagerThread(reply, "MC", this.peer));
    }

}