package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class DELRQ extends Packet {
    private String FileName;

    public DELRQ(String FileName) {
        super((short)8);
        this.FileName = FileName;
    }

    public String getFileName() {
        return FileName;
    }
}
