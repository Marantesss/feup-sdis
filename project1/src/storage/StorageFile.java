package storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;

import utils.Utils;

public class StorageFile {

    private String path;
    private String fileID;
    private int replication;
    /**
     * key = <fileID>_<ChunkNo>
     * value = number of times the chunk is stored
     */
    private ArrayList<Chunk> chunks;

    public StorageFile(String path, int replication) {
        this.path = path;
        this.replication = replication;
        this.chunks = new ArrayList<Chunk>();
        this.fileID = StorageFile.generateFileID(path);
        this.splitFile();
    }

    public static String generateFileID(String path) {
        // get owner and last modified date
        String dateModified = "";
        String owner = "";

        Path filePath = Paths.get(path);
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            dateModified = fileAttributes.lastModifiedTime().toString();
            owner = Files.getOwner(filePath).getName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] fileIDByteArray = Utils.getSHA256(path + "_" + owner + "_" + dateModified);
        String fileID = Utils.bytesToHex(fileIDByteArray);
        return fileID;
    }

    private void splitFile() {
        int nBytes;
        int chunkNo = 0;
        int size = 64000;
        byte[] buffer = new byte[size];
        File file = new File(this.path);
        
        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {

            while ((nBytes = bis.read(buffer)) > 0) {
                byte[] body = Arrays.copyOf(buffer, nBytes);
                
                Chunk chunk = new Chunk(this.fileID, chunkNo, body, nBytes, this.replication);
                this.chunks.add(chunk);

                buffer = new byte[size];

                chunkNo++;
            }

            if (file.length() % size == 0) {
                Chunk chunk = new Chunk(this.fileID, chunkNo, buffer, 0, this.replication);
                this.chunks.add(chunk);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<Chunk> getChunks() {
        return this.chunks;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Path: " + this.path + "\n");
        buf.append("File ID: " + this.fileID + "\n");
        buf.append("Desired Replication Degree: " + this.replication + "\n");
        buf.append("Chunks in this file:\n");

        for (Chunk chunk : this.chunks) {
            buf.append("ID: " + chunk.getNumber() + "\n");
            buf.append("Size: " + chunk.getSize()/1000 + "KB\n");
        }

        return buf.toString();
    }
}
