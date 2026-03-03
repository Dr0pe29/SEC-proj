package tecnico.pt;

public class Message {
    private final NetworkAddress destination;
    private final byte[] data;

    public Message(NetworkAddress destination, byte[] data) {
        this.destination = destination;
        this.data = data;
    }

    public NetworkAddress getDestination() { return destination; }
    public byte[] getData() { return data; }


}
