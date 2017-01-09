package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class WRQ extends Packet {
    private String FileName;

    public WRQ(String FileName) {
        super((short)2);
        this.FileName = FileName;
    }

    public String getFileName() {
        return FileName;
    }
}