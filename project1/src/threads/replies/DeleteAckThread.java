package threads.replies;

import peer.Peer;
import utils.Utils;

public class DeleteAckThread implements Runnable {

    private String version;

    private int senderID;

    private int initiatorID;

    private String fileID;
    
    private Peer peer;

    public DeleteAckThread(byte[] message, Peer peer) {
        // <Version> DELETEACK <SenderId> <FileId> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.version = header[0];
        this.senderID = Integer.parseInt(header[2]);
        this.initiatorID = Integer.parseInt(header[3]);
        this.fileID = header[4];

        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received DELETEACK message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        if (this.version.equals("1.0")) {
            System.out.println(this.getInfo("NOT USING ENHANCED VERSION"));
            return;
        }
        
        System.out.println(this.getInfo("Deletion acknowledgement received successfully"));

        if (this.peer.getPeerID() == this.initiatorID)
            this.peer.getStorage().addDeleteAck(this.fileID, this.senderID);
        
        this.peer.getStorage().decrementNConfirmationMessages(this.fileID, this.senderID);
    }

}