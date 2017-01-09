package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class ACK extends Packet {
    private short block;

    public ACK(short block) {
        super((short)4);
        this.block = block;
    }

    public short getBlock() {
        return block;
    }
}
