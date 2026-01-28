import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple bulletin board server implementation to demonstrate socket
 * programming concepts.
 * 
 * Key Socket Concepts Demonstrated:
 * - ServerSocket: Listens for incoming TCP connections on a specified port
 * - Socket: Represents a TCP connection to a client
 * - Multi-threading: Handles multiple client requests concurrently
 * 
 * How it works:
 * 1. Create a ServerSocket bound to a port
 * 2. Accept incoming client connections (blocking operation)
 * 3. For each client, spawn a new thread to handle the request
 * 4. The main thread immediately returns to accept() to wait for the next
 * client
 * 
 * This allows the server to serve multiple clients simultaneously.
 */
public final class Server {
    // Volatile flag to allow graceful shutdown
    private static volatile boolean running = true;

    // Board configuration per RFC Section 4.2
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final int NOTE_WIDTH = 100;
    private static final int NOTE_HEIGHT = 100;

    // Valid colors per RFC Section 4.3
    private static Set<String> getValidColors() {
        Set<String> colors = new HashSet<>();
        colors.add("yellow");
        colors.add("blue");
        colors.add("green");
        colors.add("pink");
        colors.add("orange");
        colors.add("purple");
        colors.add("white");
        return colors;
    }

    public static void main(String argv[]) throws Exception {
        // Get the port number from the command line
        int port = 4200;
        if (argv.length > 0) {
            try {
                port = Integer.parseInt(argv[0]);
                if (port < 1024 || port > 65535) {
                    System.err.println("Port must be between 1024 and 65535");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + argv[0]);
                System.exit(1);
            }
        }

        ServerSocket serverSocket = null;
        try {
            // Step 1: Create a ServerSocket
            // This socket listens for incoming TCP connection requests on the specified
            // port
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("---------------------------------------------------");

            // Create shared board instance per RFC Section 10.2
            Board board = new Board(BOARD_WIDTH, BOARD_HEIGHT, NOTE_WIDTH, NOTE_HEIGHT, getValidColors());
            System.out.println("Board initialized: " + BOARD_WIDTH + "x" + BOARD_HEIGHT +
                    " (notes: " + NOTE_WIDTH + "x" + NOTE_HEIGHT + ")");

            // Step 2: Process client requests in an infinite loop
            while (running) {
                try {
                    // socket.accept() blocks until a client connects
                    // When a client connects, it returns a Socket object representing that
                    // connection
                    Socket clientConnection = serverSocket.accept();

                    // Get client IP for logging
                    String clientIP = clientConnection.getInetAddress().getHostAddress();

                    // Step 3: Create a ClientHandler object to handle this specific request
                    // Pass the client socket and shared board to the request handler
                    ClientHandler request = new ClientHandler(clientConnection, clientIP, board);

                    // Step 4: Create a new thread to process the request
                    // This is crucial because accept() is blocking
                    // If we processed requests sequentially, the server would hang
                    // while one client was being served
                    Thread thread = new Thread(request);

                    // Step 5: Start the thread
                    // The thread will run the run() method of ClientHandler
                    thread.start();

                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                }
            }

        } catch (BindException e) {
            System.err.println("Error: Port " + port + " is already in use");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error creating ServerSocket: " + e.getMessage());
            System.exit(1);
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    System.out.println("\nServer stopped");
                } catch (IOException e) {
                    System.err.println("Error closing ServerSocket: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gracefully shut down the server (can be called from other threads)
     */
    public static void shutdown() {
        running = false;
    }
}
