package tecnico.pt.consensus.hotstuff;

public final class Quorum {
    private final int f;

    public Quorum(int f) {
        if (f < 0) throw new IllegalArgumentException("f must be >= 0");
        this.f = f;
    }

    public int size() {
        return 2 * f + 1;
    }
}