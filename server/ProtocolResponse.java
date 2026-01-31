/**
 * Protocol response following RFC
 * 
 * 
 * Responses can be either:
 * - "OK" for successful operations
 * - "ERROR <code> <message>" for failed operations with specific error codes
 * 
 * Error codes defined by RFC:
 * - INVALID_FORMAT: Malformed command syntax
 * - OUT_OF_BOUNDS: Coordinates outside board boundaries
 * - COLOR_NOT_SUPPORTED: Invalid color specification
 * - COMPLETE_OVERLAP: Note would completely overlap existing note
 * - NO_NOTE_AT_COORDINATE: No note exists at specified coordinate
 * - PIN_NOT_FOUND: Attempted to unpin non-existent pin
 */
public class ProtocolResponse {
    private final boolean success;
    private final String errorCode;
    private final String errorMessage;
    private final boolean disconnect;

    // Private constructor for creating responses
    private ProtocolResponse(boolean success, String errorCode, String errorMessage, boolean disconnect) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.disconnect = disconnect;
    }

    /**
     * successful OK response
     */
    public static ProtocolResponse ok() {
        return new ProtocolResponse(true, null, null, false);
    }

    /**
     * Successful OK response with a status message (e.g., OK NOTE_POSTED)
     */
    public static ProtocolResponse okWithStatus(String status) {
        return new ProtocolResponse(true, status, null, false);
    }

    /**
     * Successful OK response with data (e.g., GET returns "OK n" + data lines)
     */
    public static ProtocolResponse okWithData(String data) {
        return new ProtocolResponse(true, data, null, false);
    }

    /**
     * Successful OK response that signals disconnection
     */
    public static ProtocolResponse okDisconnect() {
        return new ProtocolResponse(true, null, null, true);
    }

    /**
     * ERROR response with specified code and message
     */
    public static ProtocolResponse error(String code, String message) {
        return new ProtocolResponse(false, code, message, false);
    }

    /**
     * Disconnect
     */
    public boolean isDisconnect() {
        return disconnect;
    }

    /**
     * Success!!
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * protocol response as a string
     */
    @Override
    public String toString() {
        if (success) {
            if (errorCode != null) {
                // errorCode used as status/data for OK responses
                // Check if it starts with a number (GET response format "OK n")
                if (errorCode.matches("^\\d+.*")) {
                    return errorCode; // Already contains "OK n\ndata..."
                }
                return "OK " + errorCode;
            }
            return "OK";
        } else {
            return "ERROR " + errorCode + " " + errorMessage;
        }
    }
}