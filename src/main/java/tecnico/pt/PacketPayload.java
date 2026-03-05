package tecnico.pt;
import java.io.Serializable;
import java.util.Arrays;

public class PacketPayload implements Serializable {
    public enum Type { DATA, ACK }

    private final int senderId;
    private final int destinationId;
    private final long sequenceNumber;
    private final Type type;
    private final byte[] payload;
    private final byte[] signature;

    public PacketPayload(int senderId, int destinationId, long sequenceNumber, Type type, byte[] payload) {
        this.senderId = senderId;
        this.destinationId = destinationId;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.payload = payload;
        this.signature = null;
    }

    public PacketPayload(int senderId, int destinationId, long sequenceNumber, Type type, byte[] payload, byte[] signature) {
        this.senderId = senderId;
        this.destinationId = destinationId;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.payload = payload;
        this.signature = Arrays.copyOf(signature, signature.length);
    }

    // Unique ID used by StubbornLink to identify which message to stop resending
    public String getUniqueId() {
        return senderId + ":" + sequenceNumber;
    }

    public int getSenderId()        { return senderId; }
    public int getDestinationId()   { return destinationId; }
    public long getSequenceNumber() { return sequenceNumber; }
    public Type getType()           { return type; }
    public byte[] getPayload()      { return payload; }
    public byte[] getSignature()    { return signature; }
}
