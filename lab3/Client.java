import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * java Client <host_name> <remote_object_name> <oper> <opnd>*
 * 
 * <host_name>
 *      is the name of the host where the server runs; 
 * <remote_object_name>
 *      is the name the server bound the remote object to;
 * <oper>
 *      is ''register'' or ''lookup'', depending on the operation to invoke;
 * <opnd>*
 *      is the list of operands of the specified operation:
 *          <DNS name> <IP Address>, for register;
 *          <DNS name>, for lookup.
 */
public class Client {

    // Client command arguments
    private String hostName;
    private String remoteObjectName;
    private String oper;

    private String dnsName;
    private String ipAddress;

    public static void main(String[] args) {

        Client client = new Client();

        if(!client.parseArgs(args))
            return;

        try {
            Registry registry = LocateRegistry.getRegistry(client.hostName);
            ServerInterface server = (ServerInterface) registry.lookup(client.remoteObjectName);

            switch(client.oper) {
                case "lookup":
                    String lookupResponse = server.lookup(client.dnsName);
                    System.out.println(client.oper + " " + client.dnsName + " :: " + lookupResponse);
                    break;
                case "register":
                    int registerResponse = server.register(client.dnsName, client.ipAddress);
                    System.out.println(client.oper + " " + client.dnsName + " " + client.ipAddress + " :: " + registerResponse);
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        
    }

    private boolean parseArgs(String[] args) {
        if(args.length < 3) {
            this.printUsage();
            return false;
        }

        this.hostName = args[0];
        this.remoteObjectName = args[1];
        this.oper = args[2];

        switch(oper) {
            case "lookup":
                if(args.length != 4) {
                    this.printUsage();
                    return false;
                }
                this.dnsName = args[3];
                break;
            case "register":
                if(args.length != 5) {
                    this.printUsage();
                    return false;
                }
                this.dnsName = args[3];
                this.ipAddress = args[4];
                break;
        }

        return true;
    }

    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("\tjava Client <host_name> <remote_object_name> <oper> <opnd>*");
    }
}
