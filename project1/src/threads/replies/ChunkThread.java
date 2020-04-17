package threads.replies;

import peer.Peer;
import utils.Utils;

public class ChunkThread implements Runnable {

    private int senderID;

    private String fileID;

    private int chunkNo;

    private byte[] chunkContent;

    private Peer peer;

    public ChunkThread(byte[] message, Peer peer) {
        // <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = Integer.parseInt(header[4]);

        this.chunkContent = Utils.getBody(message);
        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received CHUNK message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tChunk Number: " + this.chunkNo + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        // increment number of CHUNK messaged received for current chunk
        this.peer.incrementChunkMessagesReceived(this.fileID, this.chunkNo);
        // check if current peer requested a file restore
        // and if that chunk was already received before
        if (this.peer.hasRequestedFile(this.fileID) && !this.peer.getStorage().hasRestoredChunk(this.fileID, this.chunkNo)) {
            // store chunk
            this.peer.getStorage().putRestoredChunk(this.fileID, this.chunkNo, this.chunkContent);
            // flag chunk as received
            this.peer.flagReceivedChunk();
            System.out.println(this.getInfo("Restored chunk successfully"));
        } else {
            System.out.println(this.getInfo("Not requested file"));
        }
    }

}