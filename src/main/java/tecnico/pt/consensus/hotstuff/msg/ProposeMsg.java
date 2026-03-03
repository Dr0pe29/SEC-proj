package tecnico.pt.consensus.hotstuff.msg;

import tecnico.pt.consensus.hotstuff.model.Block;

public final class ProposeMsg implements HotStuffMessage {
    private final int view;
    private final int leaderId;
    private final Block block;

    public ProposeMsg(int view, int leaderId, Block block) {
        this.view = view;
        this.leaderId = leaderId;
        this.block = block;
    }

    @Override public MsgType type() { return MsgType.PROPOSE; }
    @Override public int view() { return view; }

    public int getLeaderId() { return leaderId; }
    public Block getBlock() { return block; }
}