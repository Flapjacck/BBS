import java.io.*;
import java.net.*;

/**
 * Handles TCP connection to the bulletin board server.
 * Sends commands and parses responses per protocol spec.
 */
public class ClientConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 4200;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;

    public ClientConnection() {
        this.connected = false;
    }

    // Establish connection to server
    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read initial board configuration from server
            String config = in.readLine();
            if (config != null && config.startsWith("BOARD_CONFIG")) {
                // Parse and store board configuration if needed
                // Format: BOARD_CONFIG width height noteWidth noteHeight colors...
                String[] parts = config.split(" ");
                if (parts.length >= 5) {
                    // Board config received successfully
                }
            }
            
            connected = true;
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    // Send command and get response
    public String sendCommand(String command) {
        if (!connected) {
            return "ERROR NOT_CONNECTED";
        }
        try {
            out.println(command);

            String response = in.readLine();
            if (response == null) {
                connected = false;
                return "ERROR CONNECTION_CLOSED";
            }

            // Handle multi-line responses (OK <count>)
            if (response.startsWith("OK ")) {
                String[] parts = response.split(" ", 2);
                if (parts.length == 2) {
                    try {
                        int count = Integer.parseInt(parts[1]);
                        StringBuilder sb = new StringBuilder();
                        sb.append(response);
                        for (int i = 0; i < count; i++) {
                            String line = in.readLine();
                            if (line != null) {
                                sb.append("\n").append(line);
                            }
                        }
                        return sb.toString();
                    } catch (NumberFormatException e) {
                        // Not a count, just return response
                    }
                }
            }
            return response;
        } catch (IOException e) {
            connected = false;
            return "ERROR " + e.getMessage();
        }
    }

    // Close connection gracefully
    public void disconnect() {
        try {
            if (connected) {
                out.println("DISCONNECT");
            }
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            // Ignore close errors
        }
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
