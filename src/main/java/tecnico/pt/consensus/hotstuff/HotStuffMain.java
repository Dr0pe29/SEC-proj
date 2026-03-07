package tecnico.pt.consensus.hotstuff;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tecnico.pt.AuthenticatedPerfectLink;
import tecnico.pt.MemberInfo;
import tecnico.pt.MembersList;
import tecnico.pt.StubbornLink;
import tecnico.pt.UDPClient;
import tecnico.pt.UDPServer;
import tecnico.pt.consensus.hotstuff.net.AuthenticatedHotStuffNetwork;
import tecnico.pt.consensus.hotstuff.service.InMemoryBlockchainService;
import tecnico.pt.crypto.AuthenticatedSignature;

public class HotStuffMain {

    public static void main(String[] args) throws Exception {

        int selfId = Integer.parseInt(args[0]);

        Map<Integer, String> members = Map.of(
            1, "member1",
            2, "member2",
            3, "member3",
            4, "member4"
        );

        MembersList membersList = new MembersList();
        MemberInfo selfInfo = membersList.getMemberInfo(selfId);

        // ===============================
        // CRYPTO SETUP
        // ===============================

        AuthenticatedSignature crypto = new AuthenticatedSignature();
        crypto.loadPrivateKey(selfId);

        for (Map.Entry<Integer, MemberInfo> entry : membersList.getAllMembers().entrySet()) {
            byte[] pubKeyBytes = entry.getValue().getPublicKey();
            if (pubKeyBytes != null && pubKeyBytes.length > 0) {
                crypto.addPublicKey(entry.getKey(), pubKeyBytes);
            }
        }

        // ===============================
        // TRANSPORT LAYER
        // ===============================

        UDPClient udpClient = new UDPClient(membersList);
        StubbornLink stubbornLink = new StubbornLink(udpClient);

        AuthenticatedPerfectLink apl =
            new AuthenticatedPerfectLink(selfId, stubbornLink, crypto);

        stubbornLink.setHigherLayer(apl);

        UDPServer udpServer = new UDPServer(
            selfInfo.getServerAddress(),
            selfInfo.getServerPort(),
            stubbornLink
        );

        // ===============================
        // CONSENSUS SETUP
        // ===============================

        HotStuffConfig config = new HotStuffConfig(selfId, members);
        HotStuffState state = new HotStuffState();
        InMemoryBlockchainService service = new InMemoryBlockchainService();

        AuthenticatedHotStuffNetwork net =
            new AuthenticatedHotStuffNetwork(selfId, apl, members);

        HotStuffNode node =
            new HotStuffNode(config, state, net, service);

        net.attachNode(node);

        // Deduplicação de pedidos do cliente recebidos por este nó
        Set<String> seenClientRequests = ConcurrentHashMap.newKeySet();

        // ===============================
        // DISPATCH: CLIENT vs CONSENSUS
        // ===============================

        apl.setUpcall(payload -> {
            if (payload == null) {
                return;
            }

            // CLIENT REQUEST
            if (payload.startsWith("CLIENT_APPEND|")) {
                String[] parts = payload.split("\\|", 4);

                if (parts.length != 4) {
                    System.err.println(
                        "[CLIENT->NODE " + selfId + "] invalid client payload: " + payload
                    );
                    return;
                }

                String clientId = parts[1];
                String reqSeq = parts[2];
                String command = parts[3];

                String requestId = clientId + ":" + reqSeq;

                if (!seenClientRequests.add(requestId)) {
                    System.out.println(
                        "[CLIENT->NODE " + selfId + "] duplicate request ignored: " + requestId
                    );
                    return;
                }

                System.out.println(
                    "[CLIENT->NODE " + selfId + "] append request: "
                    + command
                    + " requestId="
                    + requestId
                );

                node.submitCommand(new ClientRequest(requestId, command));
            }

            // CONSENSUS MESSAGE
            else {
                net.handleConsensusPayload(payload);
            }
        });

        // ===============================
        // START NETWORK
        // ===============================

        udpServer.start();
        node.start();

        System.out.println(
            "Node " + selfId + " started on "
            + selfInfo.getServerAddress()
            + ":"
            + selfInfo.getServerPort()
        );

        // mantém processo vivo
        Thread.currentThread().join();
    }
}