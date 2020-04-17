package storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import utils.Utils;

public class Chunk implements Serializable {

    /**
     * Generated Serial Version UID
     */
    private static final long serialVersionUID = 903564817024248687L;

    private String fileID;
    private int number;
    private byte[] content;
    private int size;
    private int replication;
    private int desiredReplication;

    public Chunk(String fileID, int number, byte[] content, int size, int desiredReplication) {
        this.fileID = fileID;
        this.number = number;
        this.content = content;
        this.size = size;
        this.replication = 0;
        this.desiredReplication = desiredReplication;
    }

    public String getFileID() {
        return fileID;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSize() {
        return size;
    }

    public int getReplication() {
        return replication;
    }

    public void incrementReplication() {
        this.replication++;
    }

    public void decrementReplication() {
        this.replication--;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public int getDesiredReplication() {
        return desiredReplication;
    }

    public void setDesiredReplication(int desiredReplication) {
        this.desiredReplication = desiredReplication;
    }

    public void serialize(String path) {
        String fullPath = path + Utils.getCharSeparator() + "chk_" + this.number + ".ser";

        try {
            FileOutputStream fileOut = new FileOutputStream(fullPath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static Chunk deserialize(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Chunk chunk = (Chunk) in.readObject();
            in.close();
            fileIn.close();
            return chunk;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
        
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ID: " + this.number + "\n");
        buf.append("Size: " + this.size/1000 + "KB\n");
        buf.append("Perceived Replication Degree: " + this.replication);
        return buf.toString();
    }
}
