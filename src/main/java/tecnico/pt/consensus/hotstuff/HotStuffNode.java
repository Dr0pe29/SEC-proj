package tecnico.pt.consensus.hotstuff;

import java.util.Set;

import tecnico.pt.consensus.hotstuff.model.Block;
import tecnico.pt.consensus.hotstuff.model.Ids;
import tecnico.pt.consensus.hotstuff.model.QC;
import tecnico.pt.consensus.hotstuff.model.Vote;
import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;
import tecnico.pt.consensus.hotstuff.msg.ProposeMsg;
import tecnico.pt.consensus.hotstuff.msg.VoteMsg;
import tecnico.pt.consensus.hotstuff.msg.QcForwardMsg;
import tecnico.pt.consensus.hotstuff.service.BlockchainService;

/**
 * Stage 1 / Step 3 HotStuff "happy path":
 * - leader rotation via view % n
 * - build QC from votes
 * - update highQC
 * - decide grandparent when forming QC for grandchild
 */
public final class HotStuffNode {

    private final HotStuffConfig config;
    private final HotStuffState state;
    private final HotStuffNetwork net;
    private final BlockchainService service;

    private int lastProposedView = -1;

    public HotStuffNode(HotStuffConfig config,
                        HotStuffState state,
                        HotStuffNetwork net,
                        BlockchainService service) {
        this.config = config;
        this.state = state;
        this.net = net;
        this.service = service;
    }

    public void start() {
        proposeIfLeader();
    }

    public void onMessage(int srcId, HotStuffMessage msg) {
        switch (msg.type()) {
            case PROPOSE -> onPropose(srcId, (ProposeMsg) msg);
            case VOTE -> onVote(srcId, (VoteMsg) msg);
            case QC_FORWARD -> onQcForward(srcId, (QcForwardMsg) msg);
            default -> { /* ignore */ }
        }
    }

    // --- Leader logic ---

    public void proposeIfLeader() {
        int v = state.getCurrentView();
        if (!config.isLeader(v)) return;

        if (v == lastProposedView) return;
        lastProposedView = v;

        // Parent is the block referenced by highQC
        QC high = state.getHighQC();
        String parentId = high.getBlockId();

        System.out.println("Node " + config.getSelfId() +
        " proposing view " + v +
        " parent=" + parentId);

        // Step 3: command can be placeholder (later you integrate client requests)
        String cmd = "cmd@view" + v;

        String id = Ids.blockId(parentId, v, cmd);
        Block b = new Block(id, parentId, v, cmd, high);

        state.storeBlock(b);

        net.broadcast(new ProposeMsg(v, config.getSelfId(), b));
    }

    // --- Replica logic ---

    private void onPropose(int srcId, ProposeMsg m) {
        int v = m.view();

        System.out.println("Node " + config.getSelfId() +
        " received PROPOSE view " + v +
        " from " + srcId);

        // Optional: check leader id matches expected
        if (m.getLeaderId() != config.leaderOf(v)) return;

        Block b = m.getBlock();
        System.out.println("Node " + config.getSelfId() +
        " received block " + b.toString());
        state.storeBlock(b);

        // DUVIDOSO: Supostamente no hotstuff, o node atualiza o highQC e tenta dar commit sempre que algo muda: QC formado, Block stored
        if (b.getJustify() != null) {
            state.updateHighQC(b.getJustify());
            tryDecideFromQC(state.getHighQC());
        }

        if (v < state.getCurrentView()) {
            return;
        }
        state.setCurrentView(v);

        if (!SafetyRules.safeToVote(state, b)) return;

        Vote vote = new Vote(v, b.getId(), config.getSelfId());
        System.out.println("Node " + config.getSelfId() +
        " voting for block " + b.getId() +
        " (view=" + v + ")");

        net.send(m.getLeaderId(), new VoteMsg(v, vote));
    }

    // --- Leader collects votes ---

    private void onVote(int srcId, VoteMsg m) {
        Vote vote = m.getVote();
        int v = vote.getView();

        // Only leader processes votes for that view
        if (!config.isLeader(v)) return;
        if (v != state.getCurrentView()) return;

        state.addVote(v, vote.getBlockId(), vote.getVoterId());

        if (state.voteCount(v, vote.getBlockId()) >= config.getQuorumSize()) {
            System.out.println("Node " + config.getSelfId() +
                " formed QC for view " + v +
                " block=" + vote.getBlockId());
            // Form QC
            Set<Integer> voters = state.votersSnapshot(v, vote.getBlockId());
            QC qc = new QC(v, vote.getBlockId(), voters);

            // Try decide (grandparent rule) / meio que funciona sem ele 
            state.updateHighQC(qc);
            tryDecideFromQC(qc);

            int nextView = v + 1;
            net.broadcast(new QcForwardMsg(nextView, qc));
        }
    }

    private void onQcForward(int srcId, QcForwardMsg m) {
        int nextView = m.view();

        System.out.println("Node " + config.getSelfId() + " received QC_FORWARD for view " + nextView + " from " + srcId);

        // ignora mensagens atrasadas
        if (nextView < state.getCurrentView()) return;

        // alinha view e highQC (o QC foi "forwarded")
        state.updateHighQC(m.getQc());
        state.setCurrentView(nextView);

        tryDecideFromQC(m.getQc());

    }

    /**
     * Decide the grandparent of qc.block if chain length allows.
     */
    private void tryDecideFromQC(QC qc) {
        Block b3 = state.getBlockOrNull(qc.getBlockId());
        if (b3 == null){
            System.out.println("Node " + config.getSelfId() +
                " cannot find block b3 " + qc.getBlockId() +
                " for QC at view " + qc.getView());
            
            return;
        }

        Block b2 = state.getBlockOrNull(b3.getParentId());
        if (b2 == null) {
            System.out.println("Node " + config.getSelfId() +
                " cannot find block b2 " + b3.getParentId() +
                " for block b3 " + b3.getId());
            return;
        }

        Block b1 = state.getBlockOrNull(b2.getParentId());
        if (b1 == null) {
            System.out.println("Node " + config.getSelfId() +
                " cannot find block b1 " + b2.getParentId() +
                " for block b2 " + b2.getId());
            return;
        }

        if (b1.getId().equals(state.getLastDecidedBlockId())) return;

        state.setLastDecidedBlockId(b1.getId());

        System.out.println("Node " + config.getSelfId() +
            " DECIDED block " + b1.getId());

        service.onDecide(b1);
    }
}