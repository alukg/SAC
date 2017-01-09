package bgu.spl171.net.api.packets;

import java.util.Arrays;

/**
 * Created by guy on 09/01/17.
 */
public class DATA extends Packet {
    private short packetSize;
    private short Block;
    private byte[] data;

    public DATA(short packetSize, short block, byte[] data) {
        super((short)3);
        this.packetSize = packetSize;
        this.Block = block;
        this.data = data;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public short getBlock() {
        return Block;
    }

    public byte[] getData() {
        return Arrays.copyOfRange(data,0,data.length);
    }
}
