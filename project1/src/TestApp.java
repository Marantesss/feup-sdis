import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import exceptions.BadArgumentsException;
import peer.PeerInterface;

public class TestApp {

    /**
     * <host_name>
     */
    public static String hostName;

    /**
     * <peer_ap>
     */
    public static String peerAccessPoint;

    /**
     * <sub_protocol>
     */
    public static String subProtocol;

    /**
     * <opnd_1> for sub protocol BACKUP / RESTORE / DELETE
     */
    public static String filePath;

    /**
     * <opnd_1> for sub protocol RECLAIM
     */
    public static int sizeKB;

    /**
     * <opnd_2> for sub protocol RECLAIM
     */
    public static int replicationDegree;

    /**
     * Prints program usage
     */
    public static void printUsage() {
        System.err.println("\nUsage: java TestApp <peer_ap> <sub_protocol> [ <opnd_1> | [ <opnd_2> ] ]\n");

        System.err.println("Where:\n<peer_ap>\n\tIs the peer's access point.");

        System.err.println(
                "<sub_protocol>\n\tIs the operation the peer of the backup service must execute. It can be either the triggering of the subprotocol to test, or the retrieval of the peer's internal state.");
        System.err.println("\tIn the first case it must be one of: BACKUP, RESTORE, DELETE, RECLAIM.");
        System.err.println("\tTo retrieve the internal state, the value of this argument must be STATE");

        System.err.println(
                "<opnd_1>\n\tIs either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks.");
        System.err.println(
                "\tIn the latter case, the peer should execute the RECLAIM protocol, upon deletion of any chunk. The STATE operation takes no operands.");

        System.err.println(
                "<opnd_2>\n\tThis operand is an integer that specifies the desired replication degree and applies only to the backup protocol");
    }

    /**
     * 
     * @param args
     * @throws BadArgumentsException
     */
    public static void validateArguments(String[] args) throws BadArgumentsException {
        if (args.length < 2 || args.length > 4) {
            throw new BadArgumentsException("Invalid number of arguments.");
        }

        subProtocol = args[1].toUpperCase();
        switch (subProtocol) {
            case "BACKUP":
                if (args.length != 4)
                    throw new BadArgumentsException("Invalid number of arguments for BACKUP sub protocol");
                break;
            case "RESTORE":
                if (args.length != 3)
                    throw new BadArgumentsException("Invalid number of arguments for RESTORE sub protocol");
                break;
            case "DELETE":
                if (args.length != 3)
                    throw new BadArgumentsException("Invalid number of arguments for DELETE sub protocol");
                break;
            case "RECLAIM":
                if (args.length != 3)
                    throw new BadArgumentsException("Invalid number of arguments for RECLAIM sub protocol");
                break;
            case "STATE":
                if (args.length != 2)
                    throw new BadArgumentsException("Invalid number of arguments for STATE sub protocol");
                break;
            default:
                throw new BadArgumentsException("Invalid <sub_protocol>");
        }
    }

    /**
     * Checks if file exists
     * 
     * @throws FileNotFoundException
     */
    public static void fileExists() throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists())
            throw new FileNotFoundException("File " + file + " does not exsist");
    }

    /**
     * Parses arguments
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void parseArguments(String[] args) throws FileNotFoundException {
        peerAccessPoint = args[0];
        switch (subProtocol) {
            case "BACKUP":
                filePath = args[2];
                fileExists();
                replicationDegree = Integer.parseInt(args[3]);
                break;
            case "RESTORE":
                filePath = args[2];
                fileExists();
                break;
            case "DELETE":
                filePath = args[2];
                fileExists();
                break;
            case "RECLAIM":
                sizeKB = Integer.parseInt(args[2]);
                break;
            case "STATE":
                break;
        }
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            validateArguments(args);
        } catch (BadArgumentsException e) {
            System.err.println(e.getMessage());
            printUsage();
        }

        try {
            parseArguments(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        String reply = new String();

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            PeerInterface peer = (PeerInterface) registry.lookup(peerAccessPoint);
            switch (subProtocol) {
                case "BACKUP":
                    peer.backup(filePath, replicationDegree);
                    break;
                case "RESTORE":
                    peer.restore(filePath);
                    break;
                case "DELETE":
                    peer.delete(filePath);
                    break;
                case "RECLAIM":
                    peer.reclaim(sizeKB);
                    break;
                case "STATE":
                    reply = peer.state();
                    break;
            }

        } catch (RemoteException | NotBoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println(reply);
    }
}
