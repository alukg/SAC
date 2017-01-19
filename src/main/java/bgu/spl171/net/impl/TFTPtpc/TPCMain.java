package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.BidiMessagingProtocalImp;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImp;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.packets.Packet;
import bgu.spl171.net.srv.Server;

import java.util.function.Supplier;

import static bgu.spl171.net.srv.Server.threadPerClient;

public class TPCMain {
    public static void main(String[] args) {
        Supplier<BidiMessagingProtocol<Packet>> protocolFactory = () -> new BidiMessagingProtocalImp();
        Supplier<MessageEncoderDecoder<Packet>> encoderDecoderFactory = () -> new MessageEncoderDecoderImp();
        Server<Packet> tpc = threadPerClient(7777,protocolFactory ,encoderDecoderFactory);
        tpc.serve();
    }
}
