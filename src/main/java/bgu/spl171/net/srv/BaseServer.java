package bgu.spl171.net.srv;

import bgu.spl171.net.api.ConnectionsImp;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.bidi.Connections;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    ConnectionsImp cons;

    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        cons= new ConnectionsImp();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                System.out.println("waiting for clients");
                Socket clientSock = serverSock.accept();
                System.out.println("client registered");
                BidiMessagingProtocol msgProc = protocolFactory.get();
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        msgProc);

                int conId = cons.addConnection(handler);
                msgProc.start(conId,cons);
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null) {
		    sock.close();
        }
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
