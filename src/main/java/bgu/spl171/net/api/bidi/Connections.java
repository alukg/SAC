package bgu.spl171.net.api.bidi;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
