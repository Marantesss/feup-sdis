import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;

/**
 * > java Server <port number> 
 *  <port number>
 *      is the port number the server shall use to provide the service 
 */
public class Server {

    private DatagramSocket socket;

    private int port;

    private Hashtable<String,String> table;

    public static void main(String[] args) throws IOException {

        Server server = new Server();

        server.create(args);

        while (true) {
            server.run();
        }
    }

    private void create(String[] args) throws IOException {
        this.port = Integer.parseInt(args[0]);
        this.socket = new DatagramSocket(this.port);
        this.table = new Hashtable<String, String>();
    }

    private void run() throws IOException {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        this.process(packet);
    }

    private void process(DatagramPacket packet) throws IOException {
        String received = new String(packet.getData());
        
        String message = this.parse(received);
        
        int result = -1;
        
        if (message != null) {
            result = table.size();
            
            message = result + "\n" + message;
        } else {
            message = Integer.toString(result);
        }

        byte[] buf = message.getBytes();

        int clientPort = packet.getPort();

        InetAddress clientAddress = packet.getAddress();
        
        DatagramPacket newPacket = new DatagramPacket(buf, buf.length, clientAddress, clientPort);

        socket.send(newPacket);
    }

    private String parse(String received) {
        String[] tokens = received.split(" ");

        String response;

        for(int i=0; i<tokens.length; i++) {
            tokens[i] = tokens[i].trim();
            System.out.println(tokens[i]);
        }
        
        if (tokens[0].equals("register")) {
            if (this.table.containsKey(tokens[1]) == false) {
                this.table.put(tokens[1], tokens[2]);
                response = tokens[1] + " " + tokens[2];

                return response;
            } else {

                return null;
            }
        } else if (tokens[0].equals("lookup")) {

            if (this.table.containsKey(tokens[1]) == false) {
                return null;
            } else {
                response = tokens[1] + " " + this.table.get(tokens[1]);
                
                return response;
            }
        } else {
            System.out.println("Error parsing message");

            return null;
        }

    }
}
