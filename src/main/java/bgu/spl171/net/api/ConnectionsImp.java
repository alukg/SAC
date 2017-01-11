package bgu.spl171.net.api;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.util.HashMap;

public class ConnectionsImp implements Connections {
    private HashMap<Integer,ConnectionHandler> clients;
    private int id = 1;

    public ConnectionsImp()
    {
        clients = new HashMap<>();
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

    public HashMap<Integer,ConnectionHandler> getClients() {
        return clients;
    }

    synchronized public int getID(){
        return id++;
    }

    public int addConnection(ConnectionHandler ch){
        int num = getID();
        getClients().put(num,ch);
        return num;
    }
}
