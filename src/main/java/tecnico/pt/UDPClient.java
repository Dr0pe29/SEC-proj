package tecnico.pt;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClient {
    private final DatagramSocket socket;

    public UDPClient() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public void send(Message message) throws IOException {
        NetworkAddress destination = message.getDestination();
        byte[] data = message.getData();
        InetAddress address = InetAddress.getByName(destination.getServerAddress());
        DatagramPacket packet = new DatagramPacket(
            data, 
            data.length, 
            address, 
            destination.getServerPort()
        );
        socket.send(packet);
    }
}