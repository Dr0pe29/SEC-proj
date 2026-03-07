package tecnico.pt.consensus.hotstuff.model;

import java.util.Objects;

/**
 * Minimal HotStuff block for Stage 1 / Step 3 (no crypto).
 */
public class Block {
    private final String id;
    private final String parentId;
    private final int view;
    private final String requestId;
    private final String command;
    private final QC justify;

    public Block(String id, String parentId, int view, String requestId, String command, QC justify) {

        if (id == null || id.isBlank()) throw new IllegalArgumentException("id");
        if (view < 0) throw new IllegalArgumentException("view must be >= 0");
        if (justify == null) throw new IllegalArgumentException("justify");

        this.id = id;
        this.parentId = parentId;
        this.view = view;
        this.requestId = requestId;
        this.command = command;
        this.justify = justify;

    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public int getView() {
        return view;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCommand() {
        return command;
    }

    public QC getJustify() {
        return justify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return id.equals(block.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Block{id='" + id + "', parentId='" + parentId + "', view=" + view + ", requestId='" + requestId + "', command='" + command + "'}";
    }
}