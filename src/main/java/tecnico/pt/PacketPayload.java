package tecnico.pt;
import java.io.Serializable;

public class PacketPayload implements Serializable {
    public enum Type { DATA, ACK }

    private final int senderId;
    private final long sequenceNumber;
    private final Type type;
    private final byte[] payload;
    private byte[] signature;

    public PacketPayload(int senderId, long sequenceNumber, Type type, byte[] payload) {
        this.senderId = senderId;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.payload = payload;
    }

    // Unique ID used by StubbornLink to identify which message to stop resending
    public String getUniqueId() {
        return senderId + ":" + sequenceNumber;
    }

    public byte[] getPayload() { return payload; }
    public byte[] getSignature() { return signature; }
    public int getSenderId() { return senderId; }
    public long getSequenceNumber() { return sequenceNumber; }
    public Type getType() { return type; }
    public void setSignature(byte[] sig) { this.signature = sig; }

}
