package threads;

import peer.Peer;

public class MessageSenderManagerThread implements Runnable {

    private byte[] message;

    private String channelKey;

    private Peer peer;

    public MessageSenderManagerThread(byte[] message, String channelKey, Peer peer) {
        this.message = message;
        this.channelKey = channelKey;
        this.peer = peer;
    }

    public String getInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n--- Sent message:\n");
        buf.append("\tTo: Channel " + this.channelKey + "\n");
        return buf.toString();
    }

    @Override
    public void run() {
        System.out.println(this.getInfo());
        this.peer.getChannel(this.channelKey).sendMessage(this.message);
    }
}