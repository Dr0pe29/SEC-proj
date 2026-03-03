package tecnico.pt.consensus.hotstuff.model;

import java.util.Set;

public final class Genesis {

    public static final String GENESIS_ID = "GENESIS";

    public static Block createGenesis() {
        QC genesisQC = createGenesisQC();
        return new Block(
                GENESIS_ID,
                GENESIS_ID,           // sem pai, aponta para si mesmo
                0,              // view 0
                null,           // sem comando
                genesisQC
        );
    }

    public static QC createGenesisQC() {
        return new QC(
                0,
                GENESIS_ID,
                Set.of()        // sem votos
        );
    }
}