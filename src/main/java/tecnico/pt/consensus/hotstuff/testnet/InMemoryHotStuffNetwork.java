package tecnico.pt.consensus.hotstuff.testnet;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import tecnico.pt.consensus.hotstuff.HotStuffNetwork;
import tecnico.pt.consensus.hotstuff.HotStuffNode;
import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;

public final class InMemoryHotStuffNetwork implements HotStuffNetwork {

    private final Map<Integer, HotStuffNode> nodes = new ConcurrentHashMap<>();

    // “contexto” do sender (para saber quem está a enviar)
    private final ThreadLocal<Integer> currentSender = new ThreadLocal<>();

    public void register(int nodeId, HotStuffNode node) {
        Objects.requireNonNull(node, "node");
        nodes.put(nodeId, node);
    }

    /**
     * Executa uma ação como se fosse enviada por senderId.
     * Útil para start() do nó: o nó vai broadcast/send e a rede sabe quem é o src.
     */
    public void runAs(int senderId, Runnable action) {
        currentSender.set(senderId);
        try {
            action.run();
        } finally {
            currentSender.remove();
        }
    }

    @Override
    public void send(int destId, HotStuffMessage msg) {
        Integer srcId = currentSender.get();
        if (srcId == null) {
            throw new IllegalStateException("No sender context. Use runAs(senderId, ...)");
        }
        HotStuffNode dest = nodes.get(destId);
        if (dest == null) throw new IllegalStateException("Unknown destId=" + destId);

        // Handler corre como DEST, para que reenvios usem sender correto
        runAs(destId, () -> dest.onMessage(srcId, msg));
    }

    @Override
    public void broadcast(HotStuffMessage msg) {
        Integer srcId = currentSender.get();
        if (srcId == null) {
            throw new IllegalStateException("No sender context. Use runAs(senderId, ...)");
        }

        for (var e : nodes.entrySet()) {
            int destId = e.getKey();
            HotStuffNode dest = e.getValue();

            // idem
            runAs(destId, () -> dest.onMessage(srcId, msg));
        }
}
}