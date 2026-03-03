package tecnico.pt.consensus.hotstuff.msg;

public interface HotStuffMessage {
    MsgType type();
    int view();
}