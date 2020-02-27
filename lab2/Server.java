import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * java Server <srvc_port> <mcast_addr> <mcast_port> 
 *
 * <srvc_port>
 *      is the port number where the server provides the service
 * <mcast_addr>
 *      is the IP address of the multicast group used by the server to advertise its service.
 * <mcast_port>
 *      is the multicast group port number used by the server to advertise its service.
 */
public class Server {
    
    // Advertiser
    private MulticastSocket multicastSocket;
    private int multicastPort;
    private InetAddress multicastAddress;
    private Timer advertiserTimer;

    // DNS Service from LAB1
    private DatagramSocket datagramSocket;
    private int datagramPort;

    private Hashtable<String,String> table;

    public static void main(String[] args) throws IOException {

        Server server = new Server();

        server.create(args);

        while (true) {
            server.run();
        }
    }

    private void create(String[] args) throws IOException {
        // Advertiser
        this.multicastPort = Integer.parseInt(args[2]);
        this.multicastAddress = InetAddress.getByName(args[1]);
        this.multicastSocket = new MulticastSocket(this.multicastPort);

        // DNS Service
        this.datagramPort = Integer.parseInt(args[0]);
        this.datagramSocket = new DatagramSocket(this.datagramPort);

        // Data
        this.table = new Hashtable<String, String>();

        // Create and schedule timer's task
        this.advertiserTimer = new Timer();
        TimerTask advertiserTask = new AdvertiserTask();
        this.advertiserTimer.schedule(advertiserTask, 0, 1000);
    }

    private void run() throws IOException {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.datagramSocket.receive(packet);
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

        this.datagramSocket.send(newPacket);
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

                System.out.println("register " + tokens[1] + " " + tokens[2] + " :: " + response);

                return response;
            } else {

                return null;
            }
        } else if (tokens[0].equals("lookup")) {

            if (this.table.containsKey(tokens[1]) == false) {
                return null;
            } else {
                response = tokens[1] + " " + this.table.get(tokens[1]);

                System.out.println("lookup " + tokens[1] + " :: " + response);
                
                return response;
            }
        } else {
            System.out.println("Error parsing message");

            return null;
        }

    }

    private class AdvertiserTask extends TimerTask {
        @Override
        public void run() {
            // build packet
            byte[] buf = Integer.toString(datagramPort).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, multicastAddress, multicastPort);

            // send packet
            try {
                multicastSocket.send(packet);
                System.out.println("multicast: " + packet.getAddress().toString() + " "  + packet.getPort() + ":" + datagramSocket.getLocalAddress() + " " + datagramSocket.getLocalPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}