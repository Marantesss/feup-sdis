package threads.requests;

import peer.Peer;
import storage.Chunk;
import threads.MessageSenderManagerThread;
import utils.Utils;

public class RemovedThread implements Runnable {

    private int senderID;

    private String fileID;

    private int chunkNo;

    private Peer peer;

    public RemovedThread(byte[] message, Peer peer) {
        // <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = Integer.parseInt(header[4]);

        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received REMOVED message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tChunk Number: " + this.chunkNo + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        // Update confirmation message counter for this chunk
        this.peer.getStorage().decrementNConfirmationMessages(this.fileID, this.chunkNo, this.senderID);

        // peer should not answer to their own REMOVED message
        if (this.peer.getPeerID() == this.senderID) {
            return;
        }

        // check if peer contains the removed chunk
        if (!this.peer.getStorage().contains(this.fileID, this.chunkNo)) {
            System.out.println(this.getInfo("Removed chunk not available"));
            return;
        }

        // decrement chunk's current replication degree
        Chunk removedChunk = this.peer.getStorage().getChunk(this.fileID, this.chunkNo);
        removedChunk.decrementReplication();
        
        // check if chunks replication degree is satisfiable
        if (removedChunk.getReplication() >= removedChunk.getDesiredReplication()) {
            System.out.println(this.getInfo("Removed chunk already with desired replication degree"));
            return;
        }

        // check if removed chunk was already backed up by another peer
        // get num of CHUNK messages received for chunk
        int lastReceivedMessages = this.peer.getChunkMessagesReceived(this.fileID, this.chunkNo);
        try {
            // wait a random amount of time between 0 and 400 ms
            int waitingTime = Utils.getRandomNumber(0, 400);
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // get current number of received CHUNK messages for that peer
        int thisReceivedMessages = this.peer.getChunkMessagesReceived(this.fileID, this.chunkNo);
        // if number of received messages differ, then another peer already backed up
        if (thisReceivedMessages != lastReceivedMessages) {
            System.out.println(this.getInfo("Removed chunk already backed up by another peer"));
            return;
        }

        // Reply with PUTCHUNK
        byte[] reply = this.peer.generatePutChunkMessage(removedChunk, this.peer.getProtocolVersion(), this.peer.getPeerID(), removedChunk.getFileID(), removedChunk.getNumber(), removedChunk.getDesiredReplication());
        this.peer.getScheduler().execute(new MessageSenderManagerThread(reply, "MDB", this.peer));
        System.out.println(this.getInfo("Removed chunk backed up successfully"));
    }

}