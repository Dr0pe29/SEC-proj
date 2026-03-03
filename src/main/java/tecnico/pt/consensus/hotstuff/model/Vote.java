package tecnico.pt.consensus.hotstuff.model;

import java.util.Objects;

/**
 * Minimal vote (no signature) for Stage 1 / Step 3.
 */
public final class Vote {
    private final int view;
    private final String blockId;
    private final int voterId;

    public Vote(int view, String blockId, int voterId) {
        if (view < 0) throw new IllegalArgumentException("view must be >= 0");
        if (blockId == null || blockId.isBlank()) throw new IllegalArgumentException("blockId");
        if (voterId < 0) throw new IllegalArgumentException("voterId must be >= 0");

        this.view = view;
        this.blockId = blockId;
        this.voterId = voterId;
    }

    public int getView() {
        return view;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getVoterId() {
        return voterId;
    }

    /**
     * Unique key for duplicate filtering (leader side).
     */
    public String uniqueKey() {
        return view + ":" + blockId + ":" + voterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vote)) return false;
        Vote vote = (Vote) o;
        return view == vote.view && voterId == vote.voterId && blockId.equals(vote.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(view, blockId, voterId);
    }

    @Override
    public String toString() {
        return "Vote{view=" + view + ", blockId='" + blockId + "', voterId=" + voterId + "}";
    }
}