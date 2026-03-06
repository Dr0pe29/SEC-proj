package tecnico.pt;

public interface PacketListener {
    /**
     * Observer pattern
     * Called when a raw UDP packet arrives, acts as a bridge between the UDP
     * layer and the higher-level protocol processing.
     */
    void onPacketReceived(PacketPayload packet);
}
