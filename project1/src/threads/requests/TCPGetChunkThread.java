package threads.requests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.Utils;

public class TCPGetChunkThread implements Runnable {

    private byte[] message;

    private int chunkNo;

    private int size;

    private DataInputStream dis;

    private DataOutputStream dos;

    public TCPGetChunkThread(byte[] message, String address, int port) {
        this.message = message;
        String[] header = new String(Utils.getHeader(message)).split(" ");
        this.chunkNo = Integer.parseInt(header[4]);
        this.size = message.length;

        try {
            Socket socket = new Socket(address, port);
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        
        try {
            this.dos.writeInt(this.chunkNo);

            if (!this.dis.readBoolean())
                return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            this.dos.writeInt(this.size);
            this.dos.write(this.message);
            this.dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}