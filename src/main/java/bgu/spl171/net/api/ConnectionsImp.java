package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.Connections;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImp implements Connections {
    private HashMap<String,Integer> clients;
    private int id = 1;

    public ConnectionsImp()
    {
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

    public HashMap<String, Integer> getClients() {
        return clients;
    }

    synchronized public int getID(){
        return id++;
    }
}
