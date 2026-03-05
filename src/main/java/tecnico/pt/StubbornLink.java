package tecnico.pt;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StubbornLink implements PacketListener {
    private final UDPClient client;
    //needs to be CopyOnWriteArraySet for thread safety
    private final Map<String, PacketPayload> pending = new ConcurrentHashMap<>();
    // Scheduler for retrying messages (better than thread, more robust cause it doesnt fail
    // on exceptions and more efficient cause timeouts are fixed and not based on sleep)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
    private PacketListener listener;

    public StubbornLink(UDPClient client){
        this.client = client;

        // Background task: Periodically resend everything in the buffer
        scheduler.scheduleAtFixedRate(this::resendAll, 
        500, 500, TimeUnit.MILLISECONDS);
    }

    public void setHigherLayer(PacketListener higherLayer) {
        this.listener = higherLayer;
    }

    public void stubbornSend(PacketPayload packet) {
        if (packet.getType() == PacketPayload.Type.DATA) {
            pending.put(packet.getUniqueId(), packet);
        }
        sendToUDP(packet);
    }

    private void sendToUDP(PacketPayload packet) {
        try {
            client.send(packet);
        } catch (IOException ignored) {}
    }

    public void acknowledge(String packetId) {
        pending.remove(packetId);
    }

    private void resendAll() {
        for (PacketPayload packet : pending.values()) {
            sendToUDP(packet);
        }
    }

    @Override
    public void onPacketReceived(byte[] data, NetworkAddress source) { 
       //Just pass the packet to higher layer
       System.out.println("Message received from " + source.getServerAddress() + ":" + source.getServerPort() + " with data: " + new String(data));
        if (listener != null) {
            listener.onPacketReceived(data, source);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
