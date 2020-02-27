import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * multicast: <mcast_addr> <mcast_port>: <srvc_addr> <srvc_port>
 *
 * <mcast_addr>
 *      is the IP address of the multicast group used by the server to advertise its service;
 *  <mcast_port>
 *      is the port number of the multicast group used by the server to advertise its service;
 * <srvc_addr>
 *      is the IP address where the server provides its service;
 * <srvc_port>
 *      is the port number where the server provides its service.
 */
public class Server {
    
    // Advertiser
    private MulticastSocket multicastSocket;
    private int multicastPort;
    private InetAddress multicastAddress;
    private Timer timer;

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

    private class AdvertiserTask extends TimerTask {
        public void run() {
            String portString = Integer.toString(datagramPort);
            DatagramPacket packet = new DatagramPacket(portString.getBytes(), portString.getBytes().length, multicastAddress,
                    multicastPort);

            try {
                multicastSocket.send(packet);
                String multicastPrint = "multicast: " + packet.getAddress().toString() + " "  + packet.getPort() + ":" + datagramSocket.getLocalAddress() + " " + datagramSocket.getLocalPort();
                System.out.println(multicastPrint);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}