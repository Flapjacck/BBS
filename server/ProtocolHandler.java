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
        // Ignore empty lines - return null to signal no response needed
        if (command == null || command.trim().isEmpty()) {
            return null;
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
     * GET command: GET PINS or GET [color=<color>] [contains=<x> <y>]
     * [refersTo=<substring>]
     */
    private ProtocolResponse handleGet(String[] parts) {
        // Special case: GET PINS
        if (parts.length == 2 && parts[1].equalsIgnoreCase("PINS")) {
            java.util.List<int[]> pins = board.getPins();
            StringBuilder response = new StringBuilder("OK " + pins.size());
            for (int[] pin : pins) {
                response.append("\n").append(pin[0]).append(" ").append(pin[1]);
            }
            return ProtocolResponse.okWithData(response.toString());
        }

        // Parse filters for GET notes
        String colorFilter = null;
        Integer containsX = null;
        Integer containsY = null;
        String refersTo = null;

        // Parse remaining parts for filters
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("color=")) {
                colorFilter = part.substring(6);
            } else if (part.startsWith("contains=")) {
                // contains=x y format
                try {
                    String coords = part.substring(9);
                    String[] xy = coords.split("\\s+");
                    if (xy.length >= 2) {
                        containsX = Integer.parseInt(xy[0]);
                        containsY = Integer.parseInt(xy[1]);
                    } else if (i + 1 < parts.length) {
                        // Handle "contains= x y" with space after =
                        containsX = Integer.parseInt(coords);
                        containsY = Integer.parseInt(parts[++i]);
                    }
                } catch (NumberFormatException e) {
                    return ProtocolResponse.error("INVALID_FORMAT", "Invalid contains coordinates");
                }
            } else if (part.startsWith("refersTo=")) {
                // Collect remaining text as refersTo value
                refersTo = part.substring(9);
                // Append remaining parts as they're part of the message
                while (i + 1 < parts.length) {
                    refersTo += " " + parts[++i];
                }
            }
        }

        // Get filtered notes
        java.util.List<Note> notes = board.getNotes(colorFilter, containsX, containsY, refersTo);
        StringBuilder response = new StringBuilder("OK " + notes.size());
        for (Note note : notes) {
            response.append("\n").append(note.toString());
        }
        return ProtocolResponse.okWithData(response.toString());
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

            String error = board.addPin(x, y);
            if (error != null) {
                return ProtocolResponse.error(error, "Cannot place pin");
            }

            return ProtocolResponse.okWithStatus("PIN_ADDED");

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

            String error = board.removePin(x, y);
            if (error != null) {
                return ProtocolResponse.error(error, "Cannot remove pin");
            }

            return ProtocolResponse.okWithStatus("PIN_REMOVED");

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

        board.removeUnpinnedNotes();
        return ProtocolResponse.okWithStatus("SHAKE_COMPLETE");
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