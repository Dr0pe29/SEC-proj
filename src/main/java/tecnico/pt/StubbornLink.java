package tecnico.pt;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class StubbornLink implements PacketListener {
    private final UDPClient client;
    //needs to be CopyOnWriteArraySet for thread safety
    private final Set<Message> pendingMessages = new CopyOnWriteArraySet<>(); 
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

    public void stubbornSend(Message message) {
        pendingMessages.add(message);
        
        try {
            client.send(message);

        } catch (IOException e) {
            System.out.println("Failed to send message to " + message.getDestination().getServerAddress() + ":" + message.getDestination().getServerPort());
        }
    }

    private void resendAll() {
        for (Message msg : pendingMessages) {
            try {
                client.send(msg);
            } catch (IOException e) {
                System.out.println("Failed to send message to " + msg.getDestination().getServerAddress() + ":" + msg.getDestination().getServerPort());
            }
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
    public void removeMessage(NetworkAddress dest, byte[] data) {
        pendingMessages.remove(new Message(dest, data));
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
