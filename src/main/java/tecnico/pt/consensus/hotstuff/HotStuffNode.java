package tecnico.pt.consensus.hotstuff;

import java.util.Set;

import tecnico.pt.consensus.hotstuff.model.Block;
import tecnico.pt.consensus.hotstuff.model.Ids;
import tecnico.pt.consensus.hotstuff.model.QC;
import tecnico.pt.consensus.hotstuff.model.Vote;
import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;
import tecnico.pt.consensus.hotstuff.msg.ProposeMsg;
import tecnico.pt.consensus.hotstuff.msg.VoteMsg;
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
            default -> { /* ignore */ }
        }
    }

    // --- Leader logic ---

    public void proposeIfLeader() {
        int v = state.getCurrentView();
        if (!config.isLeader(v)) return;

        // Parent is the block referenced by highQC
        QC high = state.getHighQC();
        String parentId = high.getBlockId();

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

        // Ignore old/future proposals in Step 3 (keep it simple)
        if (v != state.getCurrentView()) return;

        // Optional: check leader id matches expected
        if (m.getLeaderId() != config.leaderOf(v)) return;

        Block b = m.getBlock();
        state.storeBlock(b);

        if (!SafetyRules.safeToVote(state, b)) return;

        Vote vote = new Vote(v, b.getId(), config.getSelfId());
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
            // Form QC
            Set<Integer> voters = state.votersSnapshot(v, vote.getBlockId());
            QC qc = new QC(v, vote.getBlockId(), voters);

            // Update highQC
            state.updateHighQC(qc);

            // Try decide (grandparent rule)
            tryDecideFromQC(qc);

            // Advance to next view (happy path)
            state.setCurrentView(v + 1);

            // Next leader may propose (if it's me)
            proposeIfLeader();
        }
    }

    /**
     * Decide the grandparent of qc.block if chain length allows.
     */
    private void tryDecideFromQC(QC qc) {
        Block b3 = state.requireBlock(qc.getBlockId());
        String b2Id = b3.getParentId();
        if (b2Id == null) return;

        Block b2 = state.requireBlock(b2Id);
        String b1Id = b2.getParentId();
        if (b1Id == null) return;

        Block b1 = state.requireBlock(b1Id);

        // Avoid deciding the same block twice
        if (b1.getId().equals(state.getLastDecidedBlockId())) return;

        state.setLastDecidedBlockId(b1.getId());
        service.onDecide(b1);
    }
}