package threads.replies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import peer.Peer;
import utils.Utils;

public class TCPChunkThread implements Runnable {

    private String fileID;
    private Peer peer;
    private ServerSocket serverSocket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public TCPChunkThread(String fileID, Peer peer, ServerSocket serverSocket) {
        this.fileID = fileID;
        this.peer = peer;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {

        while (!this.serverSocket.isClosed()) {
            try {
                Socket socket = this.serverSocket.accept();
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int chunkNo = -1;
            
            try {
                chunkNo = this.dis.readInt();
                boolean hasChunk = this.peer.getStorage().hasRestoredChunk(this.fileID, chunkNo);

                this.dos.writeBoolean(!hasChunk);
                this.dos.flush();

                if (hasChunk)
                    continue;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                int size = this.dis.readInt();
    
                if (size > 0) {
                    byte[] message = new byte[size];
    
                    this.dis.readFully(message, 0, size);
    
                    String[] header = new String(Utils.getHeader(message)).split(" ");
                    String file = header[3];
                    int chunk = Integer.parseInt(header[4]);
                    byte[] chunkContent = Utils.getBody(message);
    
                    this.peer.incrementChunkMessagesReceived(file, chunkNo);
    
                    if (file.equals(this.peer.getRestoredID())) {
                        this.peer.getStorage().putRestoredChunk(file, chunk, chunkContent);
                        this.peer.flagReceivedChunk();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
		
	}

}