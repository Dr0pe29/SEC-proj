package tecnico.pt.Client;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.Signature;
import java.util.Map;
import java.util.Scanner;

import tecnico.pt.AuthenticatedPerfectLink;
import tecnico.pt.MemberInfo;
import tecnico.pt.UDPClient;
import tecnico.pt.UDPServer;
import tecnico.pt.MembersList;
import tecnico.pt.PacketPayload;
import tecnico.pt.StubbornLink;
import tecnico.pt.crypto.AuthenticatedSignature;
import tecnico.pt.crypto.KeyManager;;

public class CLI {
    private static final MembersList membersList = new MembersList();

    private final Integer memberId;
    private long requestSeq = 0;

    private StubbornLink stubbornLink;
    private AuthenticatedPerfectLink perfectLink;
    private UDPServer server;
    private UDPClient udpClient;

    public CLI(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getMemberId() {
        return this.memberId;
    }

    public void memberSetup() throws SocketException, UnknownHostException {
        // Initialize keys
        AuthenticatedSignature crypto = new AuthenticatedSignature();
        crypto.loadPrivateKey(this.memberId); // Load own private key from file

        // Load all other members' public keys from MembersList
        for (Map.Entry<Integer, MemberInfo> entry : membersList.getAllMembers().entrySet()) {
            byte[] pubKeyBytes = entry.getValue().getPublicKey();
            if (pubKeyBytes.length > 0) {
                crypto.addPublicKey(entry.getKey(), pubKeyBytes);
            }
        }

        //Client
        MemberInfo memberAddress = membersList.getMemberInfo(this.memberId);
        System.out.println("Initializing member " + getMemberId() + " with address " + memberAddress.getServerAddress() + ":" + memberAddress.getServerPort());

        this.udpClient = new UDPClient(membersList);
        this.stubbornLink = new StubbornLink(this.udpClient);
        this.perfectLink = new AuthenticatedPerfectLink(this.memberId, this.stubbornLink, crypto);
        this.stubbornLink.setHigherLayer(this.perfectLink);

        //Server
        this.server = new UDPServer(memberAddress.getServerAddress(), memberAddress.getServerPort(), this.stubbornLink);
        this.server.start();
    }

    public void readInput() throws IOException {
        this.memberSetup();
        System.out.println("Please enter a string to append to the blockchain:");
        Scanner scanner = new Scanner(System.in);
        while (true) { 
            String input = scanner.nextLine();
            switch (input.toLowerCase()) {
                case "exit" -> {
                    System.out.println("Exiting...");
                    this.server.stopServer(); // Stop the server thread
                    this.stubbornLink.shutdown(); // Stop the stubborn link scheduler
                    scanner.close();
                    return;
                }

                case "keys" -> {
                    System.out.println("Generating new RSA key pair...");
                    for (Integer id : membersList.getAllMembers().keySet()) {
                        KeyManager.generateKeys(id);
                    }
                }
                default -> {
                    long seq = ++requestSeq;
                    String payload = "CLIENT_APPEND|" + this.memberId + "|" + seq + "|" + input;

                    System.out.println("Broadcasting append request: " + input);

                    for (int destId : new int[]{1, 2, 3, 4}) {
                        PacketPayload msg = new PacketPayload(
                            this.memberId,
                            destId,
                            System.currentTimeMillis(),
                            PacketPayload.Type.DATA,
                            payload
                        );

                        this.perfectLink.send(msg);
                    }
                }
            }
        }
    }
    
}
