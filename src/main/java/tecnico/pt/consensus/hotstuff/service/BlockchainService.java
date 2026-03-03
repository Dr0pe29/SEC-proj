package tecnico.pt.consensus.hotstuff.service;

import tecnico.pt.consensus.hotstuff.model.Block;

public interface BlockchainService {
    void onDecide(Block decidedBlock);
}