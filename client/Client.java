import javax.swing.*;

/**
 * Bulletin board client launcher.
 * Connects to server and launches GUI.
 */
public class Client {
    private ClientConnection connection;
    private ClientBoard board;

    public Client() {
        connection = new ClientConnection();

        // Try to connect to server
        if (!connection.connect()) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to server at localhost:4200.\nMake sure the server is running.",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }

        // Launch the GUI with connection
        board = new ClientBoard(connection);

        // Disconnect on window close
        board.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}
