import java.rmi.Remote;

public interface ServerInterface extends Remote {

	void register(String dnsName, String ipAddress);

	String lookup(String dnsName);
}