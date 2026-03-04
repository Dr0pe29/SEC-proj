package tecnico.pt.consensus.hotstuff;

import org.junit.jupiter.api.Test;

import java.util.Map;

import tecnico.pt.consensus.hotstuff.service.InMemoryBlockchainService;
import tecnico.pt.consensus.hotstuff.testnet.InMemoryHotStuffNetwork;

import static org.junit.jupiter.api.Assertions.*;

class HotStuffInMemoryTest {

    @Test
    void happyPathConsensusWorks() {

        // 1) Membership
        Map<Integer, String> members = Map.of(
                0, "mem0",
                1, "mem1",
                2, "mem2",
                3, "mem3"
        );

        // 2) Fake in-memory network
        InMemoryHotStuffNetwork net = new InMemoryHotStuffNetwork();

        int n = members.size();

        InMemoryBlockchainService[] chains = new InMemoryBlockchainService[n];
        HotStuffNode[] nodes = new HotStuffNode[n];

        // 3) Create nodes
        for (int i = 0; i < n; i++) {
            HotStuffConfig cfg = new HotStuffConfig(i, members);
            HotStuffState st = new HotStuffState();
            chains[i] = new InMemoryBlockchainService();

            nodes[i] = new HotStuffNode(cfg, st, net, chains[i]);
            net.register(i, nodes[i]);
        }

        // 4) Start all nodes
        System.out.println("=== STARTING NODES ===");
        for (int i = 0; i < n; i++) {
            int id = i;
            net.runAs(id, () -> nodes[id].start());
        }

        System.out.println("=== FINAL SNAPSHOTS ===");

        for (int i = 0; i < n; i++) {
            System.out.println("Node " + i + " chain: " + chains[i].snapshot());
        }

        // 5) After execution, all chains must be equal
        var reference = chains[0].snapshot();

        for (int i = 1; i < n; i++) {
            assertEquals(reference, chains[i].snapshot(),
                    "All replicas must have identical blockchain state");
        }

        // 6) At least one block must have been decided
        assertTrue(reference.size() >= 1,
                "At least one block should have been decided");
    }
}