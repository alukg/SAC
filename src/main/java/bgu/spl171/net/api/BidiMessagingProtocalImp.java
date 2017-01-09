package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.packets.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BidiMessagingProtocalImp implements BidiMessagingProtocol<Packet> {

    private Connections connections;
    private int connectionId;

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Packet message) {
        connections.send(connectionId,getProccesedMessage(message));
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    private Packet getProccesedMessage(Packet message){
        switch (message.getOpCode()){
            case 1:
                return new RRQ(new String(bytes, 2, tempLen - 3, StandardCharsets.UTF_8));
            case 2:
                return new WRQ(new String(bytes, 2, tempLen - 3, StandardCharsets.UTF_8));
            case 3:
                short packetSize = bytesToShort(Arrays.copyOfRange(bytes,2,4));
                short block = bytesToShort(Arrays.copyOfRange(bytes,4,6));
                byte[] data = Arrays.copyOfRange(bytes,6,bytes.length-1);
                return new DATA(packetSize,block,data);
            case 4:
                return new ACK(bytesToShort(Arrays.copyOfRange(bytes,2,4)));
            case 5:
                short errorCode = bytesToShort(Arrays.copyOfRange(bytes,2,4));
                String errorMessage = new String(bytes, 4, tempLen - 5, StandardCharsets.UTF_8);
                return new ERROR(errorCode,errorMessage);
            case 6:
                return new DIRQ();
            case 7:
                return new LOGRQ(new String(bytes, 2, tempLen - 3, StandardCharsets.UTF_8));
            case 8:
                return new DELRQ(new String(bytes, 2, tempLen - 3, StandardCharsets.UTF_8));
            case 9:
                String bool = new String(bytes, 2, 1, StandardCharsets.UTF_8);
                boolean delOrAdd = true;
                if(bool == "1"){
                    delOrAdd = true;
                }
                else if(bool == "0"){
                    delOrAdd = false;
                }
                else{
                    throw new IllegalArgumentException("BCAST Add or Del is not boolean");
                }
                String fileName = new String(bytes, 3, tempLen - 4, StandardCharsets.UTF_8);
                return new BCAST(delOrAdd,fileName);
            case 10:
                return new DISC();
            default:
                return null;
        }
    }
}
