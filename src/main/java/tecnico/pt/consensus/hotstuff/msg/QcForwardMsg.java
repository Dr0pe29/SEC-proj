package tecnico.pt.consensus.hotstuff.msg;

import tecnico.pt.consensus.hotstuff.model.QC;

public final class QcForwardMsg implements HotStuffMessage {
    private final int next_view;   // next view
    private final QC qc;

    public QcForwardMsg(int next_view, QC qc) {
        this.next_view = next_view;
        this.qc = qc;
    }

    @Override public MsgType type() { return MsgType.QC_FORWARD; }
    @Override public int view() { return next_view; }

    public QC getQc() { return qc; }
}