package tecnico.pt.consensus.hotstuff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tecnico.pt.consensus.hotstuff.model.Block;
import tecnico.pt.consensus.hotstuff.model.Genesis;
import tecnico.pt.consensus.hotstuff.model.QC;
import tecnico.pt.consensus.hotstuff.store.BlockStore;
import tecnico.pt.consensus.hotstuff.store.InMemoryBlockStore;

/**
 * Local replica state for Stage 1 / Step 3 (no faults, no crypto).
 */
public final class HotStuffState {

    private int currentView;

    private QC highQC;
    private QC lockedQC;

    private final BlockStore blockStore;

    /**
     * Votes collected by the leader:
     * votes[view][blockId] = set(voterIds)
     */
    private final Map<Integer, Map<String, Set<Integer>>> votes = new HashMap<>();

    private String lastDecidedBlockId = null;

    public HotStuffState() {
        this.blockStore = new InMemoryBlockStore();

        Block genesis = Genesis.createGenesis();
        this.blockStore.put(genesis);

        this.highQC = genesis.getJustify();
        this.lockedQC = genesis.getJustify();

        this.currentView = 1; // start proposing from view 1

        lastDecidedBlockId = "GENESIS";
    }

    // --- View management ---
    public int getCurrentView() {
        return currentView;
    }

    public void setCurrentView(int v) {
        if (v < 1) throw new IllegalArgumentException("currentView must be >= 1");
        this.currentView = v;
    }

    // --- QCs ---
    public QC getHighQC() {
        return highQC;
    }

    public void updateHighQC(QC candidate) {
        if (candidate != null && candidate.newerThan(this.highQC)) {
            this.highQC = candidate;
        }
    }

    public QC getLockedQC() {
        return lockedQC;
    }

    public void setLockedQC(QC qc) {
        if (qc == null) throw new IllegalArgumentException("lockedQC");
        this.lockedQC = qc;
    }

    // --- Block store ---
    public BlockStore getBlockStore() {
        return blockStore;
    }

    public void storeBlock(Block b) {
        blockStore.put(b);
    }

    public Block getBlockOrNull(String id) {
        return blockStore.get(id).orElse(null);
    }

    // --- Votes / QC building ---
    public void addVote(int view, String blockId, int voterId) {
        votes
            .computeIfAbsent(view, __ -> new HashMap<>())
            .computeIfAbsent(blockId, __ -> new HashSet<>())
            .add(voterId);
    }

    public int voteCount(int view, String blockId) {
        return votes.getOrDefault(view, Map.of())
                    .getOrDefault(blockId, Set.of())
                    .size();
    }

    public Set<Integer> votersSnapshot(int view, String blockId) {
        return Set.copyOf(
            votes.getOrDefault(view, Map.of())
                 .getOrDefault(blockId, Set.of())
        );
    }

    // --- Decide tracking ---
    public String getLastDecidedBlockId() {
        return lastDecidedBlockId;
    }

    public void setLastDecidedBlockId(String blockId) {
        this.lastDecidedBlockId = blockId;
    }
}