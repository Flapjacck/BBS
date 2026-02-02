import java.io.*;
import java.net.Socket;

/**
 * ClientHandler processes requests from a single client.
 * 
 * Each client connection runs in its own thread with its own instance
 * of this class. The handler reads messages from the client and responds.
 * 
 * This is where client request processing logic will be implemented.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private String clientIP;
    private ProtocolHandler protocolHandler;

    public ClientHandler(Socket socket, String clientIP, Board board) {
        this.socket = socket;
        this.clientIP = clientIP;
        this.protocolHandler = new ProtocolHandler(board);
    }

    @Override
    public void run() {
        try {
            // Create input and output streams for communicating with the client
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()), true);

            System.out.println("Client connected: " + clientIP);

            // Send initial handshake with board configuration
            sendBoardConfiguration(output);

            // Read messages from the client
            String clientRequest;
            while ((clientRequest = input.readLine()) != null) {
                // Log the request
                System.out.println("Request from " + clientIP + ": " + clientRequest);

                // Process the command using protocol handler
                ProtocolResponse response = protocolHandler.processCommand(clientRequest);

                // Only send response if command was not ignored (null = empty line)
                if (response != null) {
                    output.println(response.toString());

                    // Handle DISCONNECT
                    if (response.isDisconnect()) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error handling client " + clientIP + ": " + e.getMessage());
        } finally {
            // Close the connection
            try {
                socket.close();
                System.out.println("Client disconnected: " + clientIP);
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    /**
     * Send board configuration to client upon connection.
     * Allows client to know board dimensions, note size, and valid colors.
     */
    private void sendBoardConfiguration(PrintWriter output) {
        // Format: BOARD_CONFIG width height noteWidth noteHeight color1,color2,...
        String config = "BOARD_CONFIG 800 600 100 100 yellow,blue,green,pink,orange,purple,white";
        output.println(config);
    }
}
