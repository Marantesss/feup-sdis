package threads.replies;

import peer.Peer;
import storage.Chunk;
import utils.Utils;

public class StoredThread implements Runnable {

    private int senderID;

    private String fileID;

    private int chunkNo;

    private Peer peer;

    public StoredThread(byte[] message, Peer peer) {
        // <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = Integer.parseInt(header[4]);

        this.peer = peer;
    }

    public String getInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received STORED message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tChunk Number: " + this.chunkNo + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        System.out.println(this.getInfo());
        // increment confirmation messages
        this.peer.getStorage().incrementNConfirmationMessages(this.fileID, this.chunkNo, this.senderID);
        // if chunk belongs to current peer's storage, increment its replication degree
        if (this.peer.getStorage().contains(this.fileID, this.chunkNo)) {
            Chunk storedChunk = this.peer.getStorage().getChunk(this.fileID, this.chunkNo);
            storedChunk.setReplication(this.peer.getStorage().getNConfirmationMessages(this.fileID, this.chunkNo));
        }
    }

}