package tecnico.pt.consensus.hotstuff.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tecnico.pt.consensus.hotstuff.model.Block;

public final class InMemoryBlockchainService implements BlockchainService {

    private final List<String> log = new ArrayList<>();

    @Override
    public synchronized void onDecide(Block decidedBlock) {
        if (decidedBlock.getCommand() != null) {
            log.add(decidedBlock.getCommand());
        }
    }

    public synchronized List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(log));
    }
}