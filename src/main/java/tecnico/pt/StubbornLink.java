package tecnico.pt;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StubbornLink implements PacketListener {

    private static final int RETRANSMIT_INTERVAL_MS = 2000;

    private final UDPClient client;
    //needs to be ConcurrentHashMap for thread safety
    private final Map<String, PacketPayload> pending = new ConcurrentHashMap<>();
    // Scheduler for retrying messages (better than thread, more robust cause it doesnt fail
    // on exceptions and more efficient cause timeouts are fixed and not based on sleep)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
    private PacketListener listener;

    public StubbornLink(UDPClient client){
        this.client = client;
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
    public void onPacketReceived(PacketPayload data) {
        
        System.out.println("[StubbornLink] Received packet " + data.getPayload());

        if (listener != null) {
            listener.onPacketReceived(data);
        }
    }

    public void acknowledge(String uniqueId) {
        pending.remove(uniqueId);
    }



    public void shutdown() {
        scheduler.shutdown();
    }

    
}
