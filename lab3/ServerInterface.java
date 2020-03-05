import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	void register();
	
	void lookup();

	String sayHello() throws RemoteException;
}
