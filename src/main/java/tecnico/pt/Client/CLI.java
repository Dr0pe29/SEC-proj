package tecnico.pt.Client;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import tecnico.pt.NetworkAddress;
import tecnico.pt.UDPClient;
import tecnico.pt.UDPServer;
import tecnico.pt.MembersList;
import tecnico.pt.PacketPayload;
import tecnico.pt.StubbornLink;

public class CLI {
    private static final MembersList membersList = new MembersList();
    private final Integer memberId;
    private StubbornLink stubbornLink;
    private UDPServer server;
    private UDPClient udpClient;

    public CLI(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getMemberId() {
        return this.memberId;
    }

    public void memberSetup() throws SocketException, UnknownHostException {
        // Initialize the UDP client and server for this member
        NetworkAddress memberAddress = membersList.getMemberAddress(this.memberId);
        System.out.println("Initializing member " + getMemberId() + " with address " + memberAddress.getServerAddress() + ":" + memberAddress.getServerPort());
        //CLIENT
        this.udpClient = new UDPClient();
        this.stubbornLink = new StubbornLink(this.udpClient);

        //SERVER
        this.server = new UDPServer(memberAddress.getServerAddress(), memberAddress.getServerPort(), this.stubbornLink);
        this.server.start(); // Start the server thread
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
                default -> {
                    System.out.println("You entered: " + input);
                    PacketPayload msg = new PacketPayload(this.memberId, System.currentTimeMillis(), PacketPayload.Type.DATA, input.getBytes());
                    this.stubbornLink.stubbornSend(msg); // Example address and port
                }
            }
        }
    }
    
}
