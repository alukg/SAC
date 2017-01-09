package bgu.spl171.net.api.packets;

/**
 * Created by guy on 09/01/17.
 */
public class ERROR extends Packet {
    private short errorCode;
    private String errorMessage;

    public ERROR(short errorCode, String errorMessage) {
        super((short)5);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
