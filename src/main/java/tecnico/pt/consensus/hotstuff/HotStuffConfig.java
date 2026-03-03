package tecnico.pt.consensus.hotstuff;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Static configuration for a HotStuff replica.
 *
 * Assumes static membership (as required by Stage 1).
 */
public final class HotStuffConfig {

    private final int selfId;
    private final Map<Integer, String> members; 
    // Map<nodeId, address>  (address pode ser "host:port" ou objeto NetworkAddress)

    private final int n;
    private final int f;
    private final int quorumSize;

    public HotStuffConfig(int selfId, Map<Integer, String> members) {
        if (members == null || members.isEmpty())
            throw new IllegalArgumentException("members cannot be empty");

        if (!members.containsKey(selfId))
            throw new IllegalArgumentException("selfId not in membership");

        this.selfId = selfId;
        this.members = Collections.unmodifiableMap(members);

        this.n = members.size();

        // Classical BFT assumption: N = 3f + 1
        this.f = (n - 1) / 3;

        this.quorumSize = 2 * f + 1;
    }

    // --- Getters ---

    public int getSelfId() {
        return selfId;
    }

    public Map<Integer, String> getMembers() {
        return members;
    }

    public int getN() {
        return n;
    }

    public int getF() {
        return f;
    }

    public int getQuorumSize() {
        return quorumSize;
    }

    // --- Leader rotation (round robin) ---

    public int leaderOf(int view) {
        if (view < 0)
            throw new IllegalArgumentException("view must be >= 0");

        // Round-robin rotation
        int index = view % n;

        // Assumindo IDs 0..n-1
        return index;
    }

    public boolean isLeader(int view) {
        return leaderOf(view) == selfId;
    }

    @Override
    public String toString() {
        return "HotStuffConfig{" +
                "selfId=" + selfId +
                ", n=" + n +
                ", f=" + f +
                ", quorum=" + quorumSize +
                '}';
    }
}