package tecnico.pt.consensus.hotstuff;

import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;

public interface HotStuffNetwork {
    void send(int destId, HotStuffMessage msg);
    void broadcast(HotStuffMessage msg);
}