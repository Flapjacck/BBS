/**
 * ClearHandler implements the CLEAR command logic.
 * 
 * Per RFC Section 7.6 - CLEAR Command:
 * Purpose: Reset the board to an empty state.
 * Syntax: CLEAR
 * 
 * Semantics:
 * - Removes all notes and all pins atomically
 * - Operation is atomic (no intermediate state visible per RFC Section 10.3)
 * 
 * Per RFC Section 10.2 - Shared Data Protection:
 * - CLEAR is executed within critical section for thread safety
 * - Atomic operation using mutual exclusion
 * 
 * Success Response (RFC Section 8.1):
 * - OK CLEAR_COMPLETE
 */
public class ClearHandler {

    /**
     * Handle the CLEAR command to reset the board.
     * 
     * @param board The shared board state
     * @param parts Parsed command parts [CLEAR]
     * @return ProtocolResponse indicating success
     */
    public static ProtocolResponse handle(Board board, String[] parts) {
        // Validate no extra parameters
        if (parts.length != 1) {
            return ProtocolResponse.error("INVALID_FORMAT",
                    "CLEAR takes no parameters");
        }

        // Clear board atomically
        // Board.clear() is synchronized internally
        board.clear();

        // Success per RFC Section 8.1
        return ProtocolResponse.okWithStatus("CLEAR_COMPLETE");
    }
}
