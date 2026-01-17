import java.net.ServerSocket;

// Simple server
public class Server {
    // Port number
    private static final int PORT = 4200;

    public static void main(String[] args) {
        try {
            // Create a ServerSocket that listens on port
            // The try statement automatically closes when done
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                // Print hello world when server starts
                System.out.println("Hello World");
                System.out.println("Server listening on port " + PORT);

                // Infinite loop to accept client connections
                while (true) {
                    // Once a client connects, it returns a Socket object for that client
                    var clientSocket = serverSocket.accept();

                    // Print a message when a client connects
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    clientSocket.close();
                }
            }
        } catch (Exception e) {
            // Print any error
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
