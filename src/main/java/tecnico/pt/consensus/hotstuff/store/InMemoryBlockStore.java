package tecnico.pt.consensus.hotstuff.store;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import tecnico.pt.consensus.hotstuff.model.Block;

public final class InMemoryBlockStore implements BlockStore {

    private final Map<String, Block> blocks = new ConcurrentHashMap<>();

    @Override
    public void put(Block b) {
        blocks.put(b.getId(), b);
    }

    @Override
    public Optional<Block> get(String blockId) {
        if (blockId == null) return Optional.empty();
        return Optional.ofNullable(blocks.get(blockId));
    }

    @Override
    public boolean contains(String blockId) {
        return blockId != null && blocks.containsKey(blockId);
    }
}