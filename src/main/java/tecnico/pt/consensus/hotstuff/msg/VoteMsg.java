package tecnico.pt.consensus.hotstuff.msg;

import tecnico.pt.consensus.hotstuff.model.Vote;

public final class VoteMsg implements HotStuffMessage {
    private final int view;
    private final Vote vote;

    public VoteMsg(int view, Vote vote) {
        this.view = view;
        this.vote = vote;
    }

    @Override public MsgType type() { return MsgType.VOTE; }
    @Override public int view() { return view; }

    public Vote getVote() { return vote; }
}