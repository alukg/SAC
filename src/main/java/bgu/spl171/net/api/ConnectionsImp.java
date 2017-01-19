package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImp implements Connections {
    private ConcurrentHashMap<Integer,ConnectionHandler> clients;
    private int id = 1;

    public ConnectionsImp()
    {
        clients = new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, Object msg) {
        ConnectionHandler ch = clients.get(connectionId);
        if(ch==null)
            return false;
        else
            ch.send(msg);
        return true;
    }

    @Override
    public void broadcast(Object msg) {
        for(int conId : clients.keySet()){
            send(conId,msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        clients.remove(connectionId);
    }

    synchronized private int getID(){
        return id++;
    }

    public int addConnection(ConnectionHandler ch){
        int num = getID();
        clients.put(num,ch);
        return num;
    }
}
