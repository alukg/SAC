package bgu.spl171.net.api;

import bgu.spl171.net.api.packets.*;
import com.sun.deploy.util.ArrayUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImp implements MessageEncoderDecoder<Packet> {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;

    @Override
    public Packet decodeNextByte(byte nextByte) {
        if (nextByte == '0') {
            return popPacket();
        }
        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(Packet message) {
        byte[] result = shortToBytes(message.getOpCode());

        switch (message.getOpCode()){
            case 1:
                RRQ RRQPack = (RRQ)message;
                return connectArrays(result,RRQPack.getFileName().getBytes());
            case 2:
                WRQ WRQPack = (WRQ)message;
                return connectArrays(result,WRQPack.getFileName().getBytes());
            case 3:
                DATA DATAPack = (DATA)message;
                byte[] tempDataArr = connectArrays(result,shortToBytes(DATAPack.getBlock()));
                return connectArrays(tempDataArr,DATAPack.getData());
            case 4:
                ACK ACKPack = (ACK)message;
                return connectArrays(result,shortToBytes(ACKPack.getBlock()));
            case 5:
                ERROR ErrorPack = (ERROR)message;
                byte[] tempErrorArr = connectArrays(result,shortToBytes(ErrorPack.getErrorCode()));
                return connectArrays(tempErrorArr,ErrorPack.getErrorMessage().getBytes());
            case 6:
                return result;
            case 7:
                LOGRQ LOGRQPack = (LOGRQ)message;
                return connectArrays(result,LOGRQPack.getUserName().getBytes());
            case 8:
                DELRQ DELRQPack = (DELRQ)message;
                return connectArrays(result,DELRQPack.getFileName().getBytes());
            case 9:
                BCAST BCASTPack = (BCAST)message;
                byte[] tempBCASTArr;
                if(BCASTPack.isDelOrAdd())
                     tempBCASTArr = connectArrays(result,"1".getBytes());
                else
                    tempBCASTArr = connectArrays(result,"0".getBytes());
                return connectArrays(tempBCASTArr,BCASTPack.getFileName().getBytes());
            case 10:
                return result;
            default:
                return null;
        }
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private Packet popPacket() {

        short opCode = bytesToShort(Arrays.copyOfRange(bytes,0,2));
        int tempLen = len;
        len = 0;

        switch (opCode) {
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

    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private byte[] connectArrays(byte[] a, byte[] b){
        byte[] result = new byte[a.length + b.length];
        for(int i=0;i<a.length;i++){
            result[i] = a[i];
        }
        int j = a.length;
        for(int i=0;i<b.length;i++){
            result[i+j] = b[i];
        }
        return result;
    }
}
