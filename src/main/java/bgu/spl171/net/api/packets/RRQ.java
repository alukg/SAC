package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class RRQ extends Packet {
    private String FileName;

    public RRQ(String FileName) {
        super((short)1);
        this.FileName = FileName;
    }

    public String getFileName() {
        return FileName;
    }
}