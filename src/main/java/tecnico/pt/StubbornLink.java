package tecnico.pt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StubbornLink implements PacketListener {

    private static final int RETRANSMIT_INTERVAL_MS = 500;

    private final UDPClient client;
    //needs to be ConcurrentHashMap for thread safety
    private final Map<String, PacketPayload> pending = new ConcurrentHashMap<>();
    // Scheduler for retrying messages (better than thread, more robust cause it doesnt fail
    // on exceptions and more efficient cause timeouts are fixed and not based on sleep)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
    private PacketListener listener;

    public StubbornLink(UDPClient client){
        this.client = client;

        // Background task: Periodically resend everything in the buffer
        scheduler.scheduleAtFixedRate(this::resendAll, 
        RETRANSMIT_INTERVAL_MS, RETRANSMIT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void setHigherLayer(PacketListener higherLayer) {
        this.listener = higherLayer;
    }

    //SEND
    public void stubbornSend(PacketPayload packet) {
        if (packet.getType() == PacketPayload.Type.DATA) {
            pending.put(packet.getUniqueId(), packet);
        }
        sendOnce(packet);
    }

    private void sendOnce(PacketPayload packet) {
        try {
            client.send(packet);
        } catch (IOException e) {
            // Best-effort: retransmit scheduler will retry
            System.err.println("[StubbornLink] Send failed for " + packet.getUniqueId() + ": " + e.getMessage());
        }
    }

    private void resendAll() {
        for (PacketPayload packet : pending.values()) {
            sendOnce(packet);
        }
    }

    //RECEIVE
    @Override
    public void onPacketReceived(byte[] data, NetworkAddress source) {
        PacketPayload packet;
        try {
            packet = deserialise(data);
        } catch (Exception e) {
            System.err.println("[StubbornLink] Dropping unreadable packet from " + source + ": " + e.getMessage());
            return;
        }

        if (packet.getType() == PacketPayload.Type.ACK) {
            String id = packet.getUniqueId();
            boolean removed = pending.remove(id) != null;
            if (removed) {
                System.out.println("[StubbornLink] ACK received, removed pending " + id);
            }
            return;
        }

        if (listener != null) {
            listener.onPacketReceived(data, source);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private static PacketPayload deserialise(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (PacketPayload) ois.readObject();
        }
    }
}
