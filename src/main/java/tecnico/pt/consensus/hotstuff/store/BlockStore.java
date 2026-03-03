package tecnico.pt.consensus.hotstuff.store;

import java.util.Optional;
import tecnico.pt.consensus.hotstuff.model.Block;

public interface BlockStore {

    void put(Block b);

    Optional<Block> get(String blockId);

    boolean contains(String blockId);
}