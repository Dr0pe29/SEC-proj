package tecnico.pt.consensus.hotstuff.net;

import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;

public interface HotStuffCodec {
    byte[] encode(int srcId, HotStuffMessage msg);
    Decoded decode(byte[] bytes);

    record Decoded(int srcId, HotStuffMessage msg) {}
}