import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {

	void register(String dnsName, String ipAddress);

	String lookup(String dnsName);
}
