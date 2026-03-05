package tecnico.pt;
import tecnico.pt.Client.CLI;
import java.io.IOException;
import java.net.SocketException;

public class App {
    public static void main(String[] args) throws SocketException, IOException {
        CLI cli = new CLI(args.length > 0 ? Integer.parseInt(args[0]) : 1); // Default to member ID 1 if no argument is provided
        cli.readInput();
    }
}
