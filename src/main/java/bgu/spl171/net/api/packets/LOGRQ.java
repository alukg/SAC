package bgu.spl171.net.api.packets;

public class LOGRQ extends Packet {
    private String userName;

    public LOGRQ(String userName) {
        super((short)7);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
