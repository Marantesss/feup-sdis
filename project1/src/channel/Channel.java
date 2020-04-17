package channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

import peer.Peer;
import threads.MessageReceiverManagerThread;

public class Channel implements Runnable {

    private InetAddress address;
    private int port;
    private Peer peer;

    public Channel(String addressString, int port, Peer peer) {
        try {
            this.address = InetAddress.getByName(addressString);
            this.port = port;
            this.peer = peer;
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(byte[] message) {
        // Open a new multicast socket, which will be used to send data.
        // try-with-resource allows us to declare resources to be used in
        // a try block with the assurance that the resources will be
        // closed when after the execution of that block.
        try (MulticastSocket socket = new MulticastSocket(this.port)) {
            // Create packet with message data
            DatagramPacket packet = new DatagramPacket(message, message.length, this.address, this.port);
            // send packet containing the message
            socket.send(packet);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Create a new Multicast socket that will allow
        // other programs to join it as well.
        try (MulticastSocket socket = new MulticastSocket(this.port)) {
            // Join the Multicast group.
            socket.joinGroup(this.address);
            // Buffer of bytes to store the incoming bytes
            // containing the information from some other peer.
            // Since the message includes:
            //  - A header: less 100 bytes
            //  - A Body: Max of 64KB = 64 000 bytes
            // A buffer of size 65KB is enough
            byte[] buffer = new byte[65 * 1000];
            // Listen for messages
            while (true) {
                // Receive packet
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                // Clean buffer and dispatch to peer's message receiver managing thread
                byte[] message = Arrays.copyOf(buffer, packet.getLength());
                this.peer.getScheduler().execute(new MessageReceiverManagerThread(message, this.peer));
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
