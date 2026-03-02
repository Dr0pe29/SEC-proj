package tecnico.pt;

public interface PacketListener {
    /**
     * Observer pattern
     * Called when a raw UDP packet arrives, acts as a bridge between the UDP
     * layer and the higher-level protocol processing.
     * @param data The raw bytes received.
     * @param source The address of the sender.
     */
    void onPacketReceived(byte[] data, NetworkAddress source);
}
