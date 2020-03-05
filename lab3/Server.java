import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements ServerInterface {

    private Hashtable<String,String> table;

    public static void main(String[] args) {
        Server server = new Server();

        server.create(args);

        while (true) {
            server.run();
        }
    }

    private void create(String[] args) {
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.bind(args[0], stub);

        this.table = new Hashtable<String, String>();
    }

    private void run() {
    }

    @Override
    public void register() {
        // TODO Auto-generated method stub

    }

    @Override
    public void lookup() {
        // TODO Auto-generated method stub

    }

    @Override
    public String sayHello() throws RemoteException {
        return "Hello, world!";
    }
}
