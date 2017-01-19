package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.api.BidiMessagingProtocalImp;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImp;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.packets.Packet;
import bgu.spl171.net.srv.Server;

import java.util.function.Supplier;

import static bgu.spl171.net.srv.Server.reactor;

public class ReactorMain {
    public static void main(String[] args) {
        Supplier<BidiMessagingProtocol<Packet>> protocolFactory = () -> new BidiMessagingProtocalImp();
        Supplier<MessageEncoderDecoder<Packet>> encoderDecoderFactory = () -> new MessageEncoderDecoderImp();
        Server<Packet> reactor = reactor(4,7777,protocolFactory ,encoderDecoderFactory);
        reactor.serve();
    }
}
