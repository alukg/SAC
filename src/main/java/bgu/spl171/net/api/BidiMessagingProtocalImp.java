package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.packets.*;
import com.sun.webkit.graphics.WCRenderQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BidiMessagingProtocalImp implements BidiMessagingProtocol<Packet> {

    private ConnectionsImp connections;
    private int connectionId;

    private LinkedBlockingQueue<DATA> readDataPacks;
    private LinkedBlockingQueue<byte[]> writeDataPacks;
    private String fileName;

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        this.connections = (ConnectionsImp) connections;
        this.connectionId = connectionId;
        readDataPacks = new LinkedBlockingQueue<>();
        writeDataPacks = new LinkedBlockingQueue<>();
    }

    @Override
    public void process(Packet message) throws FileNotFoundException {
        if (message == null) {
            connections.send(connectionId, new ERROR((short) 4, "Illegal TFTP operation - Unknown Opcode."));
        } else {
            switch (message.getOpCode()) {
                case 1:
                    RRQ RRQPack = (RRQ) message;
                    byte[] data;
                    try {
                        Path path = Paths.get("/Files", RRQPack.getFileName());
                        data = Files.readAllBytes(path);
                        int dataLength = data.length;
                        short packetCount = 0;
                        readDataPacks.clear();
                        while (dataLength > 512) {
                            byte[] ArrayToSend = Arrays.copyOfRange(data, 512 * packetCount, 512 * (packetCount + 1));
                            readDataPacks.add(new DATA((short) 512, packetCount, ArrayToSend));
                            packetCount++;
                            dataLength = dataLength - 512;
                        }
                        byte[] ArrayToSend = Arrays.copyOfRange(data, data.length - dataLength, dataLength);
                        readDataPacks.add(new DATA((short) dataLength, packetCount, ArrayToSend));
                        connections.send(connectionId, readDataPacks.poll());
                    } catch (FileNotFoundException e) {
                        connections.send(connectionId, new ERROR((short) 1, "RRQ of non-existing file."));
                    } catch (IOException e) {
                        connections.send(connectionId, new ERROR((short) 2, "Access violation - File cannot be written, read or deleted."));
                    }
                    break;
                case 2:
                    WRQ WRQPack = (WRQ) message;
                    File f = new File("/Files/" + WRQPack.getFileName());
                    try {
                        if (f.createNewFile()) {
                            fileName = WRQPack.getFileName();
                            connections.send(connectionId, new ACK((short) 0));
                        } else {
                            connections.send(connectionId, new ERROR((short) 5, "File already exists - File name exists on WRQ."));
                        }
                    } catch (IOException e) {
                        connections.send(connectionId, new ERROR((short) 2, "Access violation - File cannot be written, read or deleted."));
                    }
                case 3:
                    DATA DATAPack = (DATA) message;
                    writeDataPacks.add(DATAPack.getData());
                    connections.send(connectionId, new ACK(DATAPack.getBlock()));
                    if (DATAPack.getPacketSize() != 512) {
                        try (FileOutputStream fos = new FileOutputStream("/Files/" + fileName)) {
                            fos.write(concateBytesArray(writeDataPacks));
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                            throw new FileNotFoundException(e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case 4:
                    ACK ACKPack = (ACK) message;
                    if (readDataPacks.size() > 0)
                        connections.send(connectionId, readDataPacks.poll());
                    break;
                case 5:
                    ERROR ErrorPack = (ERROR) message;
                    byte[] tempErrorArr = connectArrays(result, shortToBytes(ErrorPack.getErrorCode()));
                    return connectArrays(tempErrorArr, ErrorPack.getErrorMessage().getBytes());
                case 6:
                    return result;
                case 7:
                    LOGRQ LOGRQPack = (LOGRQ) message;
                    if (connections.getClients().get(LOGRQPack.getUserName()) == null) {
                        connections.getClients().put(LOGRQPack.getUserName(), connections.getID());
                        connections.send(connectionId, new ACK((short) 0));
                    } else
                        connections.send(connectionId, new ERROR((short) 7, "User already logged in - Login username already connected."));
                    break;
                case 8:
                    DELRQ DELRQPack = (DELRQ) message;
                    return connectArrays(result, DELRQPack.getFileName().getBytes());
                case 9:
                    BCAST BCASTPack = (BCAST) message;
                    byte[] tempBCASTArr;
                    if (BCASTPack.isDelOrAdd())
                        tempBCASTArr = connectArrays(result, "1".getBytes());
                    else
                        tempBCASTArr = connectArrays(result, "0".getBytes());
                    return connectArrays(tempBCASTArr, BCASTPack.getFileName().getBytes());
                case 10:
                    return result;
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
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
}