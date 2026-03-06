package tecnico.pt;

public class MemberInfo {
    private final String serverAddress;
    private final int serverPort;
    private final byte[] publicKey; // Optional: Store the public key for signature verification

    public MemberInfo(String serverAddress, int serverPort, byte[] publicKey) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.publicKey = publicKey;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

}
