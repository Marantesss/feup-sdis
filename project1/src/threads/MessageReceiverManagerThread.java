package threads;

import java.util.concurrent.TimeUnit;

import threads.requests.AvailableThread;
import threads.requests.DeleteThread;
import threads.requests.GetChunkThread;
import threads.requests.PutChunkThread;
import threads.requests.RemovedThread;
import threads.replies.ChunkThread;
import threads.replies.DeleteAckThread;
import threads.replies.StoredThread;
import utils.Utils;
import peer.Peer;

public class MessageReceiverManagerThread implements Runnable {

    private byte[] message;

    private Peer peer;

    public MessageReceiverManagerThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        // get message type
        String messageType = new String(this.message).split(" ")[1];
        // pick a random waiting time between o and 400 ms
        int waitingTime = Utils.getRandomNumber(0, 400);

        // dispatch message request to working threads
        switch (messageType) {
            case "PUTCHUNK":
                this.peer.getScheduler().schedule(new PutChunkThread(message, this.peer), waitingTime, TimeUnit.MILLISECONDS);
                break;
            case "STORED":
                this.peer.getScheduler().execute(new StoredThread(message, this.peer));
                break;
            case "GETCHUNK":
                this.peer.getScheduler().execute(new GetChunkThread(message, this.peer));
                break;
            case "CHUNK":
                this.peer.getScheduler().execute(new ChunkThread(message, this.peer));
                break;
            case "DELETE":
                this.peer.getScheduler().execute(new DeleteThread(message, this.peer));
                break;
            case "DELETEACK":
                this.peer.getScheduler().execute(new DeleteAckThread(message, this.peer));
                break;
            case "AVAILABLE":
                this.peer.getScheduler().execute(new AvailableThread(message, this.peer));
                break;
            case "REMOVED":
                this.peer.getScheduler().execute(new RemovedThread(message, this.peer));
                break;
        }

    }

}