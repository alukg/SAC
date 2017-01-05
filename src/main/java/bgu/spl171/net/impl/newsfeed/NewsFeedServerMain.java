package bgu.spl171.net.impl.newsfeed;

import bgu.spl171.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl171.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl171.net.srv.Server;

public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed(); //one shared object

// you can use any server... 
//        Server.fixedThreadPool(
//                8,
//                7777, //port
//                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
//                ObjectEncoderDecoder::new //message encoder decoder factory
//        ).serve();

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                () ->  new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}
