package tecnico.pt.consensus.hotstuff.net;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import tecnico.pt.AuthenticatedPerfectLink;
import tecnico.pt.PacketPayload;
import tecnico.pt.consensus.hotstuff.HotStuffNetwork;
import tecnico.pt.consensus.hotstuff.HotStuffNode;
import tecnico.pt.consensus.hotstuff.msg.HotStuffMessage;

public final class AuthenticatedHotStuffNetwork implements HotStuffNetwork {

    private final int selfId;
    private final AuthenticatedPerfectLink link;
    private final Map<Integer, String> members;
    private final HotStuffCodec codec;
    private final AtomicLong nextSeq = new AtomicLong(1);

    private volatile HotStuffNode node;

    public AuthenticatedHotStuffNetwork(
            int selfId,
            AuthenticatedPerfectLink link,
            Map<Integer, String> members
    ) {
        this.selfId = selfId;
        this.link = link;
        this.members = members;
        this.codec = new JsonHotStuffCodec();
    }

    public void attachNode(HotStuffNode node) {
        this.node = node;
    }

    @Override
    public void send(int destId, HotStuffMessage msg) {

        byte[] encoded = codec.encode(selfId, msg);
        String payload = new String(encoded, StandardCharsets.UTF_8);

        PacketPayload packet = new PacketPayload(
                selfId,
                destId,
                nextSeq.getAndIncrement(),
                PacketPayload.Type.DATA,
                payload
        );

        link.send(packet);
    }

    @Override
    public void broadcast(HotStuffMessage msg) {
        for (Integer destId : members.keySet()) {
            if (destId != selfId) {
                send(destId, msg);
            }
        }
    }

    public void handleConsensusPayload(String payload) {
        HotStuffNode n = this.node;
        if (n == null || payload == null) return;

        try {
            //DEBUG:  System.out.println("[HS NET " + selfId + "] DELIVER payload=" + payload);
            
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            HotStuffCodec.Decoded decoded = codec.decode(bytes);

            //DEBUG: System.out.println("[HS NET " + selfId + "] DECODED srcId=" + decoded.srcId() + " msgType=" + decoded.msg().type());

            n.onMessage(decoded.srcId(), decoded.msg());
        } catch (Exception e) {
            System.err.println("[AuthenticatedHotStuffNetwork] Failed to decode payload: " + e.getMessage());
        }
    }
}