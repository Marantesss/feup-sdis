import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * > java Client <host> <port> <oper> <opnd>*
 *  <host>
 *      is the DNS name (or the IP address, in the dotted decimal format) where the server is running
 *  <port>
 *      is the port number where the server is providing service
 *  <oper>
 *      is the operation to request from the server, either "register" or "lookup"
 *  <opnd>*
 *      is the list of operands of that operation
 *      <DNS name> <IP address> for register
 *      <DNS name> for lookup 
 */
public class Client {

    private DatagramSocket socket;

    private int port;
    
    private InetAddress address;

    public static void main(String[] args) throws IOException {


        if (args.length == 0 || (args[2].equals("register") && args.length != 5) || (args[2].equals("lookup") && args.length != 4)) {
            System.out.println("Error calling method");
            System.out.println("Bad number of arguments");
            return;
        }

        Client client = new Client();

        client.create(args);

        client.sendPacket(client.buildMessage(args));

        client.receivePacket();

        client.destroy();
    }

    public void create(String[] args) {
        // create socket, port and InetAddress
        this.socket = new DatagramSocket();
        this.port = Integer.parseInt(args[1]);
        this.address = InetAddress.getByName(args[0]);
    }

    private void sendPacket(String message) {
        // create buffer
        byte[] buf = message.getBytes();
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        // send packet
        this.socket.send(packet);
    }

    private void receivePacket() {
        // create buffer
        byte[] buf = new byte[256];
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // receive packet
        this.socket.receive(packet);

        String received = new String(packet.getData());

        System.out.println(received);
    }

    private void destroy() {
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