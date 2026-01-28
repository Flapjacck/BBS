/**
 * PostHandler implements the POST command logic.
 * 
 * Per RFC Section 7.1 - POST Command:
 * Purpose: Create a new note on the board.
 * Syntax: POST <x> <y> <color> <message>
 * 
 * Parameters:
 * - <x> <y>: Upper-left coordinate of the note
 * - <color>: One of the valid colors announced by the server
 * - <message>: Arbitrary text content (remainder of line)
 * 
 * Validation Rules (RFC Section 9.1):
 * - Note must lie fully within board boundaries (OUT_OF_BOUNDS)
 * - Color must be supported by server (COLOR_NOT_SUPPORTED)
 * - Note must not completely overlap an existing note (COMPLETE_OVERLAP)
 * 
 * Success Response (RFC Section 8.1):
 * - OK NOTE_POSTED
 */
public class PostHandler {

    /**
     * Handle the POST command to create a new note.
     * 
     * @param board The shared board state
     * @param parts Parsed command parts [POST, x, y, color, message...]
     * @return ProtocolResponse indicating success or error
     */
    public static ProtocolResponse handle(Board board, String[] parts) {
        // Validate parameter count per RFC syntax: POST <x> <y> <color> <message>
        // Minimum: POST x y color message = 5 parts
        if (parts.length < 5) {
            return ProtocolResponse.error("INVALID_FORMAT",
                    "POST requires: <x> <y> <color> <message>");
        }

        try {
            // Parse coordinates
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3].toLowerCase();

            // Reconstruct message
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 4; i < parts.length; i++) {
                if (i > 4)
                    messageBuilder.append(" ");
                messageBuilder.append(parts[i]);
            }
            String message = messageBuilder.toString();

            // Validate non-negative coordinates
            if (x < 0 || y < 0) {
                return ProtocolResponse.error("OUT_OF_BOUNDS",
                        "Coordinates must be non-negative");
            }

            // Create note with fixed dimensions from board config
            Note note = new Note(x, y, board.getNoteWidth(), board.getNoteHeight(),
                    color, message);

            // Add note to board (board handles validation)
            String error = board.addNote(note);

            if (error != null) {
                // Return appropriate error per RFC Section 9.1
                switch (error) {
                    case "OUT_OF_BOUNDS":
                        return ProtocolResponse.error("OUT_OF_BOUNDS",
                                "Note exceeds board boundaries");
                    case "COLOR_NOT_SUPPORTED":
                        return ProtocolResponse.error("COLOR_NOT_SUPPORTED",
                                color + " is not a valid color");
                    case "COMPLETE_OVERLAP":
                        return ProtocolResponse.error("COMPLETE_OVERLAP",
                                "Note would completely overlap existing note");
                    default:
                        return ProtocolResponse.error("INVALID_FORMAT", error);
                }
            }

            // Success!!!!!
            return ProtocolResponse.okWithStatus("NOTE_POSTED");

        } catch (NumberFormatException e) {
            return ProtocolResponse.error("INVALID_FORMAT",
                    "Coordinates must be valid integers");
        }
    }
}
