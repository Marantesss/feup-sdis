package threads;

import java.util.concurrent.TimeUnit;

import peer.Peer;

public class ConfirmationCollectorThread implements Runnable {

    private static int MAX_TRIES = 5;
    private byte[] message;
    private int timeout;
    private int triesCounter;
    private int replicationDegree;
    private Peer peer;

    public ConfirmationCollectorThread(Peer peer, byte[] message, int replicationDegree) {
        this.peer = peer;
        this.message = message;
        this.timeout = 1;
        this.triesCounter = 1;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
        // get number of confirmation messages received from other piers
        String[] messageStrings = new String(this.message).split(" ");
        String fileID = messageStrings[3];
        int chunkNo = Integer.parseInt(messageStrings[4]);
        int receivedStoreds = this.peer.getStorage().getNConfirmationMessages(fileID, chunkNo);
        // check if number of received STORED messages does satisfy replication degree
        // and max_tries has not been reached 
        if(receivedStoreds < replicationDegree && this.triesCounter < MAX_TRIES) {
            // re-send message
            this.peer.getScheduler().execute(new MessageSenderManagerThread(message, "MDB", this.peer));
            // increment tries counter and double timeout
            this.timeout = this.timeout * 2;
            this.triesCounter++;
            // re-schedule ConfirmationCollector
            this.peer.getScheduler().schedule(this, this.timeout, TimeUnit.SECONDS);
        }
        // Print information to terminal
        if (this.triesCounter >= MAX_TRIES) {
            System.out.println("MAXIMUM TRIES REACHED FOR CHUNK: " + fileID + "_" + chunkNo);
        } else if (receivedStoreds >= replicationDegree) {
            System.out.println("Stored successfully chunk: " + fileID + "_" + chunkNo);
        }
    }

}