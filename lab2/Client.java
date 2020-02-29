import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

/**
 * java client <mcast_addr> <mcast_port> <oper> <opnd> *
 *
 * <mcast_addr>
 *      is the IP address of the multicast group used by the server to advertise its service;
 * <mcast_port>
 *      is the port number of the multicast group used by the server to advertise its service;
 * <oper>
 *      is ''register'' or ''lookup'', depending on the operation to invoke;
 * <opnd>*
 *      is the list of operands of the specified operation:
 *          <DNS name> <IP address>, for register;
 *          <IP address>, for lookup.
 */
public class Client {

    private MulticastSocket socket;

    private int mcastPort;
    
    private InetAddress mcastAddress;

    private int dgramPort;

    private InetAddress dgramAddress;

    public static void main(String[] args) throws IOException {

        Client client = new Client();

        client.create(args);

        client.sendPacket(client.buildMessage(args));

        client.receivePacket();

        client.destroy();
    }

    private void create(String[] args) throws IOException {
        // create socket, multiPort and InetAddress
        this.mcastPort = Integer.parseInt(args[1]);
        this.socket = new MulticastSocket(this.mcastPort);
        this.mcastAddress = InetAddress.getByName(args[0]);

        this.socket.joinGroup(this.mcastAddress);

        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        this.socket.receive(packet);

        String[] tokens = new String(packet.getData()).split(" ");

        this.dgramAddress = InetAddress.getByName(tokens[0].trim());
        this.dgramPort = Integer.parseInt(tokens[1].trim());

        System.out.println("port: " + dgramPort + " address: " + dgramAddress);
    }

    private void sendPacket(String message) throws IOException {
        // create buffer
        byte[] buf = message.getBytes();
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.dgramAddress, this.dgramPort);
        // send packet
        this.socket.send(packet);
    }

    private void receivePacket() throws IOException {
        // create buffer
        byte[] buf = new byte[256];
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // receive packet
        this.socket.receive(packet);

        String received = new String(packet.getData());

        System.out.println(received);
    }

    private void destroy() throws IOException {
        this.socket.leaveGroup(this.mcastAddress);
        this.socket.close();
    }

    private String buildMessage(String[] args) {
        String message = args[2];

        for(int i = 3; i < args.length; i++) {
            message += " " + args[i];
        }

        return message;
    }
}
