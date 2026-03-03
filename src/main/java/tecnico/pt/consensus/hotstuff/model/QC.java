package tecnico.pt.consensus.hotstuff.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Quorum Certificate: proof that a quorum voted for (blockId, view).
 * Step 3: store only voter ids (no signatures).
 */
public final class QC {
    private final int view;
    private final String blockId;
    private final Set<Integer> voterIds; // immutable snapshot

    public QC(int view, String blockId, Set<Integer> voterIds) {
        if (view < 0) throw new IllegalArgumentException("view must be >= 0");
        if (blockId == null || blockId.isBlank()) throw new IllegalArgumentException("blockId");
        if (voterIds == null) throw new IllegalArgumentException("voterIds");
        this.view = view;
        this.blockId = blockId;
        this.voterIds = Collections.unmodifiableSet(new HashSet<>(voterIds));
    }

    public int getView() {
        return view;
    }

    public String getBlockId() {
        return blockId;
    }

    public Set<Integer> getVoterIds() {
        return voterIds;
    }

    public int size() {
        return voterIds.size();
    }

    public boolean isFor(String blockId, int view) {
        return this.view == view && this.blockId.equals(blockId);
    }

    /**
     * Choose "newer" QC: by view (or height, if you add height later).
     */
    public boolean newerThan(QC other) {
        if (other == null) return true;
        return this.view > other.view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QC)) return false;
        QC qc = (QC) o;
        return view == qc.view && blockId.equals(qc.blockId) && voterIds.equals(qc.voterIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(view, blockId, voterIds);
    }

    @Override
    public String toString() {
        return "QC{view=" + view + ", blockId='" + blockId + "', voters=" + voterIds.size() + "}";
    }
}