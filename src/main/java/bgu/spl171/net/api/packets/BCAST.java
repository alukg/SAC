package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class BCAST extends Packet {
    private boolean delOrAdd;
    private String fileName;

    public BCAST(boolean delOrAdd, String fileName) {
        super((short)9);
        this.delOrAdd = delOrAdd;
        this.fileName = fileName;
    }

    public boolean isDelOrAdd() {
        return delOrAdd;
    }

    public String getFileName() {
        return fileName;
    }
}
