/**
 * ProtocolHandler processes commands
 * 
 * Handles all seven defined commands:
 * - POST: Create a new note on the board
 * - GET: Retrieve notes with optional filtering
 * - PIN: Place a pin at specified coordinates
 * - UNPIN: Remove a pin from specified coordinates
 * - SHAKE: Randomly rearrange all notes on board
 * - CLEAR: Remove all notes and pins from board
 * - DISCONNECT: Cleanly terminate client connection
 * 
 * Each command is parsed and validated for proper syntax before processing.
 * Invalid commands return INVALID_FORMAT error response.
 */
public class ProtocolHandler {
    private final Board board;

    /**
     * Create a ProtocolHandler with the shared board.
     * 
     * @param board The shared board state
     */
    public ProtocolHandler(Board board) {
        this.board = board;
    }

    /**
     * Process a single command from client and return appropriate response.
     * 
     * @param command Raw command string from client
     * @return ProtocolResponse indicating success/failure and any error details
     */
    public ProtocolResponse processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return ProtocolResponse.error("INVALID_FORMAT", "Empty command");
        }

        // Split command into parts for parsing
        String[] parts = command.trim().split("\\s+");
        String commandType = parts[0].toUpperCase();

        // Route to appropriate command handler
        switch (commandType) {
            case "POST":
                return PostHandler.handle(board, parts);
            case "GET":
                return handleGet(parts);
            case "PIN":
                return handlePin(parts);
            case "UNPIN":
                return handleUnpin(parts);
            case "SHAKE":
                return handleShake(parts);
            case "CLEAR":
                return ClearHandler.handle(board, parts);
            case "DISCONNECT":
                return handleDisconnect(parts);
            default:
                return ProtocolResponse.error("INVALID_FORMAT", "Unknown command: " + commandType);
        }
    }

    /**
     * GET command: GET [color=<color>] [contains=<text>] [refersTo=<x>,<y>]
     */
    private ProtocolResponse handleGet(String[] parts) {
        // TODO: Implement note retrieval with filtering
        return ProtocolResponse.ok();
    }

    /**
     * PIN command: PIN <x> <y>
     */
    private ProtocolResponse handlePin(String[] parts) {
        if (parts.length != 3) {
            return ProtocolResponse.error("INVALID_FORMAT", "PIN requires: x y");
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            // TODO: Implement pin placement logic
            return ProtocolResponse.ok();

        } catch (NumberFormatException e) {
            return ProtocolResponse.error("INVALID_FORMAT", "Invalid numeric coordinates");
        }
    }

    /**
     * UNPIN command: UNPIN <x> <y>
     */
    private ProtocolResponse handleUnpin(String[] parts) {
        if (parts.length != 3) {
            return ProtocolResponse.error("INVALID_FORMAT", "UNPIN requires: x y");
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            // TODO: Implement pin removal logic
            return ProtocolResponse.ok();

        } catch (NumberFormatException e) {
            return ProtocolResponse.error("INVALID_FORMAT", "Invalid numeric coordinates");
        }
    }

    /**
     * SHAKE command: SHAKE
     */
    private ProtocolResponse handleShake(String[] parts) {
        if (parts.length != 1) {
            return ProtocolResponse.error("INVALID_FORMAT", "SHAKE takes no parameters");
        }

        // TODO: Implement note shuffling logic
        return ProtocolResponse.ok();
    }

    /**
     * DISCONNECT command: DISCONNECT
     */
    private ProtocolResponse handleDisconnect(String[] parts) {
        if (parts.length != 1) {
            return ProtocolResponse.error("INVALID_FORMAT", "DISCONNECT takes no parameters");
        }

        return ProtocolResponse.okDisconnect();
    }
}