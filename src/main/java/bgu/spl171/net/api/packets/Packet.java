package bgu.spl171.net.api.packets;

public class Packet {
    private short opCode;

    public Packet(short opCode) {
        this.opCode = opCode;
    }

    public short getOpCode() {
        return opCode;
    }

    public void setOpCode(short newOpCode) {
        opCode = newOpCode;
    }
}
