package tecnico.pt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import tecnico.pt.crypto.AuthenticatedSignature;

public class AuthenticatedPerfectLink implements PacketListener {
    private final int selfId;
    private final StubbornLink stubbornLink;
    private final AuthenticatedSignature crypto;
    private final Map<Integer, Long> sequenceNumbers = new ConcurrentHashMap<>();
    private final Set<String> delivered = Collections.synchronizedSet(new HashSet<>());

    // The "Upcall" callback to the HotStuff consensus layer
    private Consumer<String> upcall;


    public AuthenticatedPerfectLink(int selfId, StubbornLink stubbornLink, AuthenticatedSignature crypto) {
        this.selfId = selfId;
        this.stubbornLink = stubbornLink;
        this.crypto = crypto;
    }

    public void setUpcall(Consumer<String> upcall) {
        this.upcall = upcall;
    }

    public void send(PacketPayload data) {
        long seq = sequenceNumbers.getOrDefault(data.getDestinationId(), 0L);
        sequenceNumbers.put(data.getDestinationId(), seq + 1);

        //Need to change the seq number, maybe change later
        PacketPayload signedData = new PacketPayload(
            data.getSenderId(),
            data.getDestinationId(),
            seq,
            data.getType(),
            data.getPayload()
        );
        try {
            //AUTHENTICATE
            signedData.setSignature(crypto.sign(Serializer.serialise(signedData)));
            stubbornLink.stubbornSend(signedData);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onPacketReceived(PacketPayload packet) {

        try {

            //VERIFYAUTH
            byte[] signature = packet.getSignature();
            if (signature == null) {
                System.err.println("[APL] Received unsigned packet from " + packet.getSenderId());
                return;
            }
            packet.setSignature(null);
            if (!crypto.verify(packet.getSenderId(), Serializer.serialise(packet), signature)){
                System.out.println("[APL] Auth failed from " + packet.getSenderId());
                return;
            }
            packet.setSignature(signature);

            //If its an ACK, stop resending the original message
            if (packet.getType() == PacketPayload.Type.ACK) {
                String originalId = packet.getDestinationId() + ":" + packet.getSequenceNumber();
                stubbornLink.acknowledge(originalId);
            // If it's a DATA message, ACK back and deliver to upper layer (if not duplicate)
            } else {
                sendAck(packet);
                //Deduplicate, fails if receives a duplicate 
                if (delivered.add(packet.getUniqueId())) {
                    if (upcall != null) {
                        // Deliver the payload to the upper layer
                        upcall.accept(packet.getPayload());
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendAck(PacketPayload original) throws Exception {

        PacketPayload ack = new PacketPayload(
            selfId,                      
            original.getSenderId(),      
            original.getSequenceNumber(),
            PacketPayload.Type.ACK,      
            "ACK"                      
        );
        
        ack.setSignature(crypto.sign(Serializer.serialise(ack)));
        stubbornLink.stubbornSend(ack); 
    }
}