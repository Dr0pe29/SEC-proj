package tecnico.pt;
import tecnico.pt.Client.CLI;
import java.io.IOException;
import java.net.SocketException;

public class App {
    public static void main(String[] args) throws SocketException, IOException {
        CLI cli = new CLI(args.length > 0 ? args[0] : "member1"); // Default to "member1" if no argument is provided
        cli.memberSetup(); // Set up the member's client and server
        cli.readInput();
    }
}
