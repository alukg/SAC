package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.Connections;

import java.util.HashMap;

public class ConnectionsImp implements Connections {
    HashMap<String,Integer> clients;

    public ConnectionsImp(){
        clients = new HashMap<>();
    }

    @Override
    public boolean send(int connectionId, Object msg) {

    }

    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
