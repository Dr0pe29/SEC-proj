package tecnico.pt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class UDPClient {
    private final DatagramSocket socket;
    private final MembersList membersList;

    public UDPClient(MembersList membersList) throws SocketException {
        this.socket = new DatagramSocket();
        this.membersList = membersList;
    }

    public void send(PacketPayload payload) throws IOException {
        NetworkAddress destination = membersList.getMemberAddress(payload.getDestinationId());
        if (destination == null) {
            throw new IOException("Unknown destinationId: " + payload.getDestinationId());
        }

        byte[] data = serialise(payload);
        InetAddress address = InetAddress.getByName(destination.getServerAddress());
        DatagramPacket packet = new DatagramPacket(data, data.length, address, destination.getServerPort());
        socket.send(packet);
    }

    private static byte[] serialise(PacketPayload payload) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(payload);
            return baos.toByteArray();
        }
    }
}