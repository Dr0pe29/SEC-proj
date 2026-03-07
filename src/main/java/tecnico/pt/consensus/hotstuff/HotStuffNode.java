package tecnico.pt.consensus.hotstuff;

import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 * - leader rotation per view
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

    private final Queue<ClientRequest> pendingCommands = new ConcurrentLinkedQueue<>();
    private final Set<String> pendingIds = ConcurrentHashMap.newKeySet();
    private final Set<String> committedIds = ConcurrentHashMap.newKeySet();

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

    public void submitCommand(ClientRequest request) {
        if (request == null) return;
        if (request.command() == null || request.command().isBlank()) return;

        // already decided
        if (committedIds.contains(request.requestId())) return;

        // already pending locally
        if (!pendingIds.add(request.requestId())) return;

        pendingCommands.offer(request);
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

        // Parent is the block referenced by highQC
        QC high = state.getHighQC();
        String parentId = high.getBlockId();


        ClientRequest req = pendingCommands.poll();
        if (req == null) {
            return; // nada para propor nesta view
        }

        String cmd = req.command();
        String requestId = req.requestId();

        // remove from local pending set because it is now being proposed
        pendingIds.remove(requestId);

        lastProposedView = v;

        System.out.println("Node " + config.getSelfId() +
            " proposing view " + v +
            " parent=" + parentId +
            "requestId=" + requestId +
            " cmd=" + cmd +  "\n");

        String id = Ids.blockId(parentId, v, cmd);
        Block b = new Block(id, parentId, v, requestId, cmd, high);

        state.storeBlock(b);

        System.out.println("Node " + config.getSelfId() +
        " PROPOSING view " + v +
        " parent=" + parentId);

        net.broadcast(new ProposeMsg(v, config.getSelfId(), b));
    }

    // --- Replica logic ---

    private void onPropose(int srcId, ProposeMsg m) {
        int v = m.view();

        System.out.println("Node " + config.getSelfId() +
        " received PROPOSE view " + v +
        " from " + srcId);

        if (m.getLeaderId() != config.leaderOf(v)) return;

        Block b = m.getBlock();
        System.out.println("Node " + config.getSelfId() +
        " received block " + b.toString());
        state.storeBlock(b);

        // if this request was pending locally, remove it now
        if (b.getRequestId() != null) {
            pendingIds.remove(b.getRequestId());
            removePendingRequestById(b.getRequestId());
        }

        if (b.getJustify() != null) {
            state.updateHighQC(b.getJustify());
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

            state.updateHighQC(qc);

            decideBlock(vote.getBlockId());

            int nextView = v + 1;
            net.broadcast(new QcForwardMsg(nextView, qc));
        }
    }

    private void onQcForward(int srcId, QcForwardMsg m) {
        int nextView = m.view();

        System.out.println("Node " + config.getSelfId() + " received QC_FORWARD for view " + nextView + " from " + srcId);

        if (nextView < state.getCurrentView()) return;

        state.updateHighQC(m.getQc());
        decideBlock(m.getQc().getBlockId());
        state.setCurrentView(nextView);
        proposeIfLeader();

    }

    private void decideBlock(String blockId) {
        Block decided = state.getBlockOrNull(blockId);
        if (decided == null) return;

        if (decided.getId().equals(state.getLastDecidedBlockId())) return;

        state.setLastDecidedBlockId(decided.getId());

        System.out.println("Node " + config.getSelfId() + " DECIDED command: " + decided.getCommand());

        service.onDecide(decided);

        System.out.println("Node " + config.getSelfId() + " decided log = " + service.snapshot());
    }

    private void removePendingRequestById(String requestId) {
        pendingCommands.removeIf(req -> req.requestId().equals(requestId));
    }
}