package storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import peer.Peer;
import utils.Utils;

public class Storage {

    /**
     * key = <fileID>_<ChunkNo> value = number of times the chunk is stored
     */
    private ConcurrentHashMap<String, Chunk> chunks;
    private ConcurrentHashMap<String, byte[]> restored;
    private ArrayList<StorageFile> files;
    private ConcurrentHashMap<String, ArrayList<Integer>> confirmationMessages;
    private ConcurrentHashMap<String, ArrayList<Integer>> deleteAcks;
    private Peer peer;
    private int capacity;
    private String backupPath;
    private String restorePath;

    public Storage(Peer peer) {
        this.chunks = new ConcurrentHashMap<String, Chunk>();
        this.restored = new ConcurrentHashMap<String, byte[]>();
        this.files = new ArrayList<StorageFile>();
        this.confirmationMessages = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.deleteAcks = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.peer = peer;
        this.capacity = 100 * 1000 * 1000; // 100 MB
        this.backupPath = "peer_" + this.peer.getPeerID() + Utils.getCharSeparator() + "backup";
        this.restorePath = this.backupPath.replace("backup", "restore");
    }

    public Storage(Peer peer, ConcurrentHashMap<String, Chunk> chunks) {
        this.chunks = chunks;
        this.restored = new ConcurrentHashMap<String, byte[]>();
        this.files = new ArrayList<StorageFile>();
        this.confirmationMessages = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.deleteAcks = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.peer = peer;
        this.capacity = 100 * 1000 * 1000; // 100 MB
        this.backupPath = "peer_" + this.peer.getPeerID() + Utils.getCharSeparator() + "backup";
        this.restorePath = this.backupPath.replace("backup", "restore");
    }

    public void init() {
        String path = "peer_" + this.peer.getPeerID();

        File dir = new File(path);
        File backup = new File(backupPath);
        File restore = new File(restorePath);

        dir.mkdir();
        backup.mkdir();
        restore.mkdir();
    }

    public static Storage read(String path, Peer peer) {
        ConcurrentHashMap<String, Chunk> chunks = new ConcurrentHashMap<String, Chunk>();

        File backup = new File(path + peer.getPeerID() + Utils.getCharSeparator() + "backup");

        if (backup.listFiles() != null)
            for (File file : backup.listFiles()) {
                for (File entry : file.listFiles()) {
                    Chunk chunk = Chunk.deserialize(entry.getPath());

                    chunks.put(chunk.getFileID() + "_" + chunk.getNumber(), chunk);
                }
            }

        return new Storage(peer, chunks);
    }

    public void addFile(StorageFile file) {
        this.files.add(file);
    }

    public void addChunk(Chunk chunk) {
        String fileID = chunk.getFileID();

        this.chunks.put(fileID + "_" + chunk.getNumber(), chunk);

        File dir = new File(backupPath + Utils.getCharSeparator() + fileID);

        if (!dir.exists())
            dir.mkdir();

        chunk.serialize(backupPath + Utils.getCharSeparator() + fileID);
    }

    public Chunk getChunk(String fileID, int chunkNo) {
        String key = fileID + "_" + chunkNo;
        return this.chunks.get(key);
    }

    public ConcurrentHashMap<String, Chunk> getChunks() {
        return this.chunks;
    }

    private ConcurrentHashMap<Integer, byte[]> getChunks(String fileID) {
        ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<Integer, byte[]>();

        for (Map.Entry<String, byte[]> entry : restored.entrySet()) {
            String key = entry.getKey();

            if (key.contains(fileID))
                chunks.put(Integer.parseInt(key.substring(key.indexOf("_") + 1)), entry.getValue());
        }

        return chunks;
    }

    public boolean hasRestoredChunk(String fileID, int chunkNo) {
        return this.restored.containsKey(fileID + "_" + chunkNo);
    }

    public void putRestoredChunk(String fileID, int chunkNo, byte[] content) {
        String key = fileID + "_" + chunkNo;
        this.restored.put(key, content);
    }

    public void restore(String fileID, String name) {
        String path = restorePath + Utils.getCharSeparator() + name;

        File file = new File(path);

        ConcurrentHashMap<Integer, byte[]> chunks = getChunks(fileID);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            for (byte[] chunk : chunks.values())
                bos.write(chunk);

            bos.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public int getNConfirmationMessages(String fileID, int chunkNo) {
        // DOES NOT WORK, blocks sometimes :(
        // int ret = this.confirmationMessages.get(fileID + "_" + chunkNo).size();
        for (Map.Entry<String, ArrayList<Integer>> entry : this.confirmationMessages.entrySet()) {
            if (entry.getKey().equals(fileID + "_" + chunkNo)) {
                return entry.getValue().size();
            }
        }
        return 0;
    }

    public void incrementNConfirmationMessages(String fileID, int chunkNo, int peerID) {
        String key = fileID + '_' + chunkNo;

        if (!this.confirmationMessages.containsKey(key))
            this.confirmationMessages.put(key, new ArrayList<Integer>());

        this.confirmationMessages.get(key).add(peerID);
    }

    public void decrementNConfirmationMessages(String fileID, int peerID) {
        for (Map.Entry<String, ArrayList<Integer>> entry : this.confirmationMessages.entrySet()) {
            if (entry.getKey().contains(fileID))
                entry.getValue().remove(peerID);
        }
    }

    public void decrementNConfirmationMessages(String fileID, int chunkNo, int peerID) {
        String key = fileID + '_' + chunkNo;

        for (Map.Entry<String, ArrayList<Integer>> entry : this.confirmationMessages.entrySet()) {
            if (entry.getKey().contains(key))
                entry.getValue().remove(peerID);
            return;
        }
    }

    public void addDeleteAck(String fileID, int peerID) {
        if (!this.deleteAcks.containsKey(fileID))
            this.deleteAcks.put(fileID, new ArrayList<Integer>());

        this.deleteAcks.get(fileID).add(peerID);
    }

    public ArrayList<String> getToDelete(int peerID) {
        ArrayList<String> toDelete = new ArrayList<String>();

        for (Map.Entry<String, ArrayList<Integer>> entry : confirmationMessages.entrySet()) {
            if (entry.getValue().contains(peerID)) {
                String key = entry.getKey();
                String fileID = key.substring(0, key.indexOf('_'));

                for (Map.Entry<String, ArrayList<Integer>> entry2 : deleteAcks.entrySet()) {
                    if (entry2.getKey().equals(fileID) && !entry2.getValue().contains(peerID)) {
                        toDelete.add(fileID);
                        break;
                    }
                }
            }
        }

        return toDelete;
    }

    public boolean contains(String fileID, int chunkNo) {
        String key = fileID + "_" + chunkNo;
        return this.chunks.containsKey(key);
    }

    public int getUsedSpace() {
        int space = 0;

        for (Chunk chunk : this.chunks.values())
            space += chunk.getSize();

        return space;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ArrayList<StorageFile> getFiles() {
        return files;
    }

    public boolean hasAvailableSpace(int chunkSize) {
        return this.capacity > (this.getUsedSpace() + chunkSize);
    }

    public void deleteChunks(String fileID) {
        // remove all chunks with file id equals to fileID
        this.chunks.values().removeIf(chunk -> chunk.getFileID().equals(fileID));

        // delete files in fileID folder
        File dir = new File(backupPath + Utils.getCharSeparator() + fileID);
        String[] chunkFilesDir = dir.list();
        for (String chunkFileDir : chunkFilesDir) {
            File chunkFile = new File(dir.getPath(), chunkFileDir);
            chunkFile.delete();
        }

        // finally delete folder
        dir.delete();
    }

    public void deleteChunk(Chunk chunk) {
        this.chunks.remove(chunk.getFileID() + "_" + chunk.getNumber());

        char separator = Utils.getCharSeparator();

        String path = backupPath + separator + chunk.getFileID() + separator + "chk_" + chunk.getNumber() + ".ser";

        File file = new File(path);

        file.delete();
    }

	public String getChunksInformation() {
        StringBuffer buf = new StringBuffer();
        
        for (Chunk chunk : this.chunks.values()) {
            buf.append(chunk.toString());
            buf.append("\n");
        }

        return buf.toString();
	}
    
    @Override
    public String toString() {
        return "Maximum capacity: " + this.capacity/1000 + " KB\nUsed storage: " + this.getUsedSpace()/1000 + "KB";
    }
}
