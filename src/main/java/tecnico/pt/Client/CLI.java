package tecnico.pt.Client;
import java.util.Scanner;

public class CLI {
    public static void main(String[] args) {
        System.out.println("Please enter a string to append to the blockchain:");
        Scanner scanner = new Scanner(System.in);
        while (true) { 
            String input = scanner.nextLine();
            switch (input.toLowerCase()) {
                case "exit" -> {
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                }
                default -> System.out.println("You entered: " + input);
            }
        }
        
    }
}
