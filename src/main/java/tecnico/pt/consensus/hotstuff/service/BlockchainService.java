package tecnico.pt.consensus.hotstuff.service;

import java.util.List;

import tecnico.pt.consensus.hotstuff.model.Block;

public interface BlockchainService {
    void onDecide(Block decidedBlock);
    List<String> snapshot();
}