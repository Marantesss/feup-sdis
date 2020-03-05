import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements ServerInterface {

    private Hashtable<String,String> table;

    public static void main(String[] args) throws RemoteException {
        Server server = new Server();

        server.create(args);
    }

    private void create(String[] args) throws RemoteException {
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(args[0], stub);

        this.table = new Hashtable<String, String>();
    }

    @Override
    public int register(String dnsName, String ipAddress) throws RemoteException {
        int result = -1;

        if (this.table.containsKey(dnsName) == false) {
            this.table.put(dnsName, ipAddress);

            result = this.table.size();
        }
        
        System.out.println("register " + dnsName + " " + ipAddress + " :: " + result);

        return result;
    }

    @Override
    public String lookup(String dnsName) throws RemoteException {
        if (this.table.containsKey(dnsName) == false) {
            return null;
        } else {
            String ipAddress = dnsName + " " + this.table.get(dnsName);

            System.out.println("lookup " + dnsName + " :: " + ipAddress);
            
            return ipAddress;
        }
    }
}
