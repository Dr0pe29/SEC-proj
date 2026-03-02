package tecnico.pt;

public class NetworkAddress {
    private final String serverAddress;
    private final int serverPort;

    public NetworkAddress(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }
}
