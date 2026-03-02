package tecnico.pt.Client;
import java.io.IOException;
import java.lang.reflect.Member;
import java.net.SocketException;
import java.util.Scanner;

import tecnico.pt.NetworkAddress;
import tecnico.pt.UDPClient;
import tecnico.pt.UDPServer;
import tecnico.pt.MembersList;

public class CLI {
    private static final MembersList membersList = new MembersList();
    private final String memberId;
    private UDPClient client;
    private UDPServer server;

    public CLI(String memberId) {
        this.memberId = memberId;
        this.client = null;
        this.server = null;
    }
    
    public String getMemberId() {
        return this.memberId;
    }

    public void memberSetup() throws SocketException {
        // Initialize the UDP client and server for this member
        System.out.println("Initializing member " + getMemberId() + " with address " + MembersList.MEMBERS.get(getMemberId()).getServerAddress() + ":" + MembersList.MEMBERS.get(getMemberId()).getServerPort());
        this.client = new UDPClient();
        this.server = new UDPServer(MembersList.MEMBERS.get(memberId).getServerPort(), (data, source) -> {
            //temporary packet listener implementation for testing
            System.out.println("Received packet from " + source.getServerAddress() + ":" + source.getServerPort());
            System.out.println("Data: " + new String(data));
        });
        this.server.start(); // Start the server thread
    }

    public void readInput() throws IOException {
        System.out.println("Please enter a string to append to the blockchain:");
        Scanner scanner = new Scanner(System.in);
        while (true) { 
            String input = scanner.nextLine();
            switch (input.toLowerCase()) {
                case "exit" -> {
                    System.out.println("Exiting...");
                    this.server.stopServer(); // Stop the server thread
                    scanner.close();
                    return;
                }
                default -> {
                    System.out.println("You entered: " + input);
                    // Here you would add the logic to send the input to the server
                    client.send(input.getBytes(), new NetworkAddress("localhost", 12346)); // Example address and port
                }
            }
        }
    }
    
}
