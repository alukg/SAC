package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.packets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

public class BidiMessagingProtocalImp implements BidiMessagingProtocol<Packet> {

    private boolean shouldTerminate = false;

    private Connections connections;
    private int connectionId;
    private static ConcurrentHashMap<Integer, String> activeClients = new ConcurrentHashMap<>();
    private static ConcurrentLinkedQueue<String> writingProcess = new ConcurrentLinkedQueue<>();

    private LinkedBlockingQueue<DATA> readDataPacks;
    private LinkedBlockingQueue<byte[]> writeDataPacks;
    private String fileName;

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        this.connectionId = connectionId;
        readDataPacks = new LinkedBlockingQueue<>();
        writeDataPacks = new LinkedBlockingQueue<>();
    }

    @Override
    public void process(Packet message) throws FileNotFoundException {
        if (message == null) {
            connections.send(connectionId, new ERROR((short) 4, "Illegal TFTP operation - Unknown Opcode."));
        } else if (message.getOpCode() == 7) {
            LOGRQ LOGRQPack = (LOGRQ) message;
            if (activeClients.containsValue(LOGRQPack.getUserName()) || activeClients.containsKey(connectionId)) {
                connections.send(connectionId, new ERROR((short) 7, "User already logged in - Login username already connected."));
            } else {
                activeClients.put(connectionId, LOGRQPack.getUserName());
                connections.send(connectionId, new ACK((short) 0));
            }
        } else {
            if (activeClients.containsKey(connectionId)) {
                switch (message.getOpCode()) {
                    case 1:
                        RRQ RRQPack = (RRQ) message;
                        byte[] data = null;
                        try {
                            Path path = Paths.get("Files", RRQPack.getFileName());
                            boolean canRead;
                            synchronized (activeClients) {
                                canRead = !writingProcess.contains(RRQPack.getFileName());
                                if (canRead)
                                    data = Files.readAllBytes(path);
                            }
                            if (canRead)
                                sendDataPacks(data);
                        } catch (FileNotFoundException e) {
                            connections.send(connectionId, new ERROR((short) 1, "File not found - RRQ of non-existing file."));
                        } catch (IOException e) {
                            connections.send(connectionId, new ERROR((short) 2, "Access violation - File cannot be written, read or deleted."));
                        }
                        break;
                    case 2:
                        WRQ WRQPack = (WRQ) message;
                        File f = new File("Files/" + WRQPack.getFileName());
                        try {
                            writeDataPacks.clear();
                            boolean isCreated;
                            synchronized (activeClients) {
                                isCreated = !writingProcess.contains(WRQPack.getFileName()) && f.createNewFile();
                                if (isCreated) {
                                    fileName = WRQPack.getFileName();
                                    writingProcess.add(fileName);
                                }
                            }
                            if (isCreated)
                                connections.send(connectionId, new ACK((short) 0));
                            else
                                connections.send(connectionId, new ERROR((short) 5, "File already exists - File name exists on WRQ."));
                        } catch (IOException e) {
                            connections.send(connectionId, new ERROR((short) 2, "Access violation - File cannot be written, read or deleted."));
                        }
                        break;
                    case 3:
                        DATA DATAPack = (DATA) message;
                        writeDataPacks.add(DATAPack.getData());
                        connections.send(connectionId, new ACK(DATAPack.getBlock()));
                        if (DATAPack.getPacketSize() != 512) {
                            try (FileOutputStream fos = new FileOutputStream("Files/" + fileName)) {
                                fos.write(concateBytesArray(writeDataPacks));
                                for(ConcurrentHashMap.Entry<Integer,String> client : activeClients.entrySet())
                                    connections.send(client.getKey(), new BCAST(true, fileName));
                                writingProcess.remove(fileName);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                throw new FileNotFoundException(e.getMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 4:
                        ACK ACKPack = (ACK) message;
                        if (readDataPacks.size() > 0)
                            connections.send(connectionId, readDataPacks.poll());
                        break;
                    case 6:
                        String result = "";
                        File folder = new File("Files");
                        File[] listOfFiles = folder.listFiles();
                        if (listOfFiles != null && listOfFiles.length > 0) {
                            LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].isFile()) {
                                    byte[] file = listOfFiles[i].getName().getBytes();
                                    byte[] connected = new byte[file.length+1];
                                    for(int j=0;j<file.length;j++){
                                        connected[j]=file[j];
                                    }
                                    connected[connected.length-1] = '\n';
                                    queue.add(connected);
                                }
                            }
                            byte[] resultBytes = concateBytesArray(queue);
                            sendDataPacks(resultBytes);
                        }
                        if(listOfFiles != null && listOfFiles.length == 0){
                            LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
                            byte[] file = "There is no files".getBytes();
                            byte[] connected = new byte[file.length+1];
                            for(int j=0;j<file.length;j++){
                                connected[j]=file[j];
                            }
                            connected[connected.length-1] = '\n';
                            queue.add(file);
                            byte[] resultBytes = concateBytesArray(queue);
                            sendDataPacks(resultBytes);
                        }
                        break;
                    case 8:
                        DELRQ DELRQPack = (DELRQ) message;
                        try {
                            String tempFile = "Files/" + DELRQPack.getFileName();
                            File fileTemp = new File(tempFile);
                            boolean canDelete;
                            synchronized (activeClients) {
                                canDelete = !writingProcess.contains(DELRQPack.getFileName()) && fileTemp.delete();
                            }
                            if (canDelete) {
                                connections.send(connectionId, new ACK((short) 0));
                                for(ConcurrentHashMap.Entry<Integer,String> client : activeClients.entrySet())
                                    connections.send(client.getKey(), new BCAST(false,DELRQPack.getFileName()));
                            } else {
                                connections.send(connectionId, new ERROR((short) 1, "File not found - DELRQ of non-existing file."));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        activeClients.remove(connectionId);
                        connections.send(connectionId, new ACK((short) 0));
                        connections.disconnect(connectionId);
                        shouldTerminate = true;
                        break;
                    default:
                        connections.send(connectionId, new ERROR((short) 4, "Illegal TFTP operation - Unknown Opcode."));
                        break;
                }
            } else
                connections.send(connectionId, new ERROR((short) 6, "User not logged in."));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private byte[] concateBytesArray(LinkedBlockingQueue<byte[]> byteArr) {
        int byteLength = 0;
        for (byte[] arr : byteArr) {
            byteLength += arr.length;
        }
        byte[] concByte = new byte[byteLength];

        int j = 0;
        for (byte[] arr : byteArr) {
            for (int i = 0; i < arr.length; i++) {
                concByte[j] = arr[i];
                j++;
            }
        }
        return concByte;
    }

    private void sendDataPacks(byte[] data) {
        int dataLength = data.length;
        short packetCount = 0;
        readDataPacks.clear();
        while (dataLength > 512) {
            byte[] ArrayToSend = Arrays.copyOfRange(data, 512 * packetCount, 512 * (packetCount + 1));
            readDataPacks.add(new DATA((short) 512, (short)(packetCount + 1), ArrayToSend));
            packetCount++;
            dataLength = dataLength - 512;
        }
        byte[] ArrayToSend = Arrays.copyOfRange(data, data.length - dataLength, data.length);
        readDataPacks.add(new DATA((short) dataLength, (short)(packetCount + 1), ArrayToSend));
        connections.send(connectionId, readDataPacks.poll());
    }

}