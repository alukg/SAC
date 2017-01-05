package bgu.spl171.net.impl.rci;

import java.io.Serializable;

public interface Command<T> extends Serializable {

    Serializable execute(T arg);
}
