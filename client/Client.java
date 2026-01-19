
import javax.swing.*;

// Simple bulletin board client launcher
public class Client {

    public Client() {
        // Launch the bulletin board
        new Board();
    }

    // Main entry point
    public static void main(String[] args) {
        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new Client());
    }
}
