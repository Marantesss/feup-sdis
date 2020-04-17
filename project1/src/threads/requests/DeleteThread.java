package threads.requests;

import peer.Peer;
import threads.MessageSenderManagerThread;
import utils.Utils;

public class DeleteThread implements Runnable {

    private String version;

    private int senderID;

    private String fileID;

    private Peer peer;

    public DeleteThread(byte[] message, Peer peer) {
        // <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.version = header[0];
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];

        this.peer = peer;
    }

    public String getInfo(String outcome) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Received DELETE message:\n");
        buf.append("\tFrom: Peer " + this.senderID + "\n");
        buf.append("\tFile Hash: " + this.fileID + "\n");
        buf.append("\tOutcome: " + outcome + "\n");
        return buf.toString();
    }


    @Override
    public void run() {
        // delete all chunks
        this.peer.getStorage().deleteChunks(this.fileID);
        System.out.println(this.getInfo("File chunks deleted successfully"));

        if (this.version.equals("2.0")) {
			byte[] message = this.peer.generateDeleteAckMessage(this.version, this.peer.getPeerID(), this.senderID, this.fileID);
			this.peer.getScheduler().execute(new MessageSenderManagerThread(message, "MC", this.peer));
		}

    }

}