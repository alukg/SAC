package bgu.spl171.net.api;

import bgu.spl171.net.api.packets.*;
import com.sun.deploy.util.ArrayUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImp implements MessageEncoderDecoder<Packet> {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0, opCounter = 2, dataSizeCounter = 2, dataCounter = 0;
    private short packetNum =-1;
    Boolean dataBool = false;
    @Override
    public Packet decodeNextByte(byte nextByte) {
        switch (packetNum) {
            case -1:
                if (opCounter > 0) {
                    pushByte(nextByte);
                    opCounter--;
                }
                if (opCounter == 0) {
                    packetNum = bytesToShort(Arrays.copyOfRange(bytes, 0, 2));
                    System.out.println("packet number " + packetNum);
                    if (packetNum == 6 || packetNum == 10)
                        return popPacket();
                    else if(packetNum != 1 && packetNum != 2 && packetNum != 7 && packetNum != 8 && packetNum != 3 && packetNum != 4)
                        return popPacket();
                }
                break;
            case 1:
            case 2:
            case 7:
            case 8:
                if (nextByte == '0') {
                    return popPacket();
                }
                pushByte(nextByte);
                break;
            case 3:
                if (dataSizeCounter > 0){
                    pushByte(nextByte);
                    dataSizeCounter--;
                }
                if (dataSizeCounter == 0){
                    dataCounter = bytesToShort(Arrays.copyOfRange(bytes, 2, 4)) + 2;
                    dataSizeCounter--;
                }
                else if (dataSizeCounter < 0 && dataCounter > 0){
                    pushByte(nextByte);
                    dataCounter--;
                    if (dataCounter == 0)
                        return popPacket();
                }
                break;
            case 4:
                pushByte(nextByte);
                opCounter++;
                if (opCounter == 2)
                    return popPacket();
                break;
        }
        return null;
    }

    @Override
    public byte[] encode(Packet message) {
        byte[] result = shortToBytes(message.getOpCode());
        byte[] ans;

        switch (message.getOpCode()){
            case 3:
                DATA DATAPack = (DATA)message;
                result = connectArrays(result,shortToBytes(DATAPack.getPacketSize()));
                result = connectArrays(result,shortToBytes(DATAPack.getBlock()));
                return connectArrays(result,DATAPack.getData());
            case 4:
                ACK ACKPack = (ACK)message;
                return connectArrays(result,shortToBytes(ACKPack.getBlock()));
            case 5:
                ERROR ErrorPack = (ERROR)message;
                byte[] tempErrorArr = connectArrays(result,shortToBytes(ErrorPack.getErrorCode()));
                ans = connectArrays(tempErrorArr,ErrorPack.getErrorMessage().getBytes());
                return connectArrays(ans,"0".getBytes());
            case 9:
                BCAST BCASTPack = (BCAST)message;
                byte[] tempBCASTArr;
                if(BCASTPack.isDelOrAdd())
                     tempBCASTArr = connectArrays(result,"1".getBytes());
                else
                    tempBCASTArr = connectArrays(result,"0".getBytes());
                ans = connectArrays(tempBCASTArr,BCASTPack.getFileName().getBytes());
                return connectArrays(ans,"0".getBytes());
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
        opCounter = 2;
        packetNum = -1;
        dataSizeCounter = 2;
        dataCounter = 0;
        dataBool = false;

        switch (opCode) {
            case 1:
                return new RRQ(new String(bytes, 2, tempLen - 2, StandardCharsets.UTF_8));
            case 2:
                return new WRQ(new String(bytes, 2, tempLen - 2, StandardCharsets.UTF_8));
            case 3:
                short packetSize = bytesToShort(Arrays.copyOfRange(bytes,2,4));
                short block = bytesToShort(Arrays.copyOfRange(bytes,4,6));
                byte[] data = Arrays.copyOfRange(bytes,6,bytes.length-1);
                return new DATA(packetSize,block,data);
            case 4:
                return new ACK(bytesToShort(Arrays.copyOfRange(bytes,2,4)));
            case 6:
                return new DIRQ();
            case 7:
                return new LOGRQ(new String(bytes, 2, tempLen - 2, StandardCharsets.UTF_8));
            case 8:
                return new DELRQ(new String(bytes, 2, tempLen - 2, StandardCharsets.UTF_8));
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
