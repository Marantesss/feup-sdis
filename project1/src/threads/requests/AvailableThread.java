package threads.requests;

import java.util.ArrayList;

import peer.Peer;
import threads.MessageSenderManagerThread;
import utils.Utils;

public class AvailableThread implements Runnable {

    private String version;

    private int senderID;
    
    private Peer peer;

    public AvailableThread(byte[] message, Peer peer) {
        // <Version> AVAILABLE <SenderId> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.version = header[0];
        this.senderID = Integer.parseInt(header[2]);

        this.peer = peer;
    }
    
    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received AVAILABLE message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }

	@Override
    public void run() {
        if (this.version.equals("1.0")) {
            System.out.println(this.getInfo("NOT USING ENHANCED VERSION"));
            return;
        }

        System.out.println(this.getInfo("Availability announcement received successfully"));

        ArrayList<String> toDelete = this.peer.getStorage().getToDelete(this.senderID);

        for (String fileID : toDelete) {
            byte[] message = this.peer.generateDeleteMessage(this.version, this.senderID, fileID);
            this.peer.getScheduler().execute(new MessageSenderManagerThread(message, "MC", this.peer));
        }
    }

}