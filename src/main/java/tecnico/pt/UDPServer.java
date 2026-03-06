package tecnico.pt;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPServer extends Thread {
    private final DatagramSocket socket;
    private final PacketListener listener;
    private boolean running;

    public UDPServer(String ipAddress, int port, PacketListener listener) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(new InetSocketAddress(InetAddress.getByName(ipAddress), port));
        this.listener = listener;
        this.running = true;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65535]; // Max UDP packet size
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Blocks until a packet arrives

                // Extract only the relevant bytes
                byte[] receivedData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, receivedData, 0, packet.getLength());

                PacketPayload payload = Serializer.deserialise(receivedData);

                // Pass the raw bytes up to the next layer
                listener.onPacketReceived(payload);

            } catch (IOException e) {
                if (running) {
                    this.stopServer(); // Stop the server if an IOException occurs while running
                }
            }
        }
    }

    public void stopServer() {
        running = false;
        socket.close();
    }
}