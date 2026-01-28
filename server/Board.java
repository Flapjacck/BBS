import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Board manages the shared bulletin board state with thread-safe operations.
 * 
 * Per RFC Section 10 - Concurrency and Synchronization:
 * - Thread-per-client model with shared board state
 * - All modifying operations (POST, PIN, UNPIN, SHAKE, CLEAR) are synchronized
 * - Atomic operations ensure no intermediate state visible to clients
 * 
 * Per RFC Section 4.2 - Board Properties:
 * - Width and height fixed at server startup
 * - Notes must lie fully inside the board
 * - Board state exists only during server execution (non-persistent)
 */
public class Board {
    private final int width;
    private final int height;
    private final int noteWidth;
    private final int noteHeight;
    private final Set<String> validColors;

    // Protected resources per RFC Section 10.2
    private final List<Note> notes;
    private final List<int[]> pins; // Each pin is [x, y]

    /**
     * Create a new board with specified dimensions and configuration.
     * 
     * @param width      Board width in pixels
     * @param height     Board height in pixels
     * @param noteWidth  Fixed width for all notes
     * @param noteHeight Fixed height for all notes
     * @param colors     Valid color palette for notes
     */
    public Board(int width, int height, int noteWidth, int noteHeight, Set<String> colors) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColors = new HashSet<>(colors);
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();
    }

    // Getters for board configuration
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNoteWidth() {
        return noteWidth;
    }

    public int getNoteHeight() {
        return noteHeight;
    }

    public Set<String> getValidColors() {
        return new HashSet<>(validColors);
    }

    /**
     * Check if a color is valid per RFC Section 9.1 - COLOR_NOT_SUPPORTED.
     */
    public boolean isValidColor(String color) {
        return validColors.contains(color.toLowerCase());
    }

    /**
     * Add a note to the board. Thread-safe.
     * Per RFC Section 7.1 - POST validation and creation.
     * 
     * @param note The note to add
     * @return null on success, error message on failure
     */
    public synchronized String addNote(Note note) {
        // Check bounds per RFC Section 9.1 - OUT_OF_BOUNDS
        if (!note.isWithinBounds(width, height)) {
            return "OUT_OF_BOUNDS";
        }

        // Check color per RFC Section 9.1 - COLOR_NOT_SUPPORTED
        if (!isValidColor(note.getColor())) {
            return "COLOR_NOT_SUPPORTED";
        }

        // Check overlap per RFC Section 9.1 - COMPLETE_OVERLAP
        for (Note existing : notes) {
            if (note.overlapsCompletely(existing)) {
                return "COMPLETE_OVERLAP";
            }
        }

        notes.add(note);
        return null; // Success
    }

    /**
     * Get all notes, optionally filtered. Thread-safe.
     * Per RFC Section 7.2 - GET with filters.
     * 
     * @param colorFilter Filter by color (null = no filter)
     * @param containsX   Filter by containing point X (null = no filter)
     * @param containsY   Filter by containing point Y (null = no filter)
     * @param refersTo    Filter by message substring (null = no filter)
     * @return List of matching notes
     */
    public synchronized List<Note> getNotes(String colorFilter, Integer containsX,
            Integer containsY, String refersTo) {
        List<Note> result = new ArrayList<>();

        for (Note note : notes) {
            // Apply all filters & logic per RFC Section 7.2
            if (colorFilter != null && !note.getColor().equalsIgnoreCase(colorFilter)) {
                continue;
            }
            if (containsX != null && containsY != null && !note.contains(containsX, containsY)) {
                continue;
            }
            if (refersTo != null && !note.getMessage().contains(refersTo)) {
                continue;
            }
            result.add(note);
        }

        return result;
    }

    /**
     * Get all pins. Thread-safe.
     * Per RFC Section 7.2 - GET PINS returns all pin coordinates.
     * 
     * @return List of [x, y] pin coordinates
     */
    public synchronized List<int[]> getPins() {
        return new ArrayList<>(pins);
    }

    /**
     * Clear all notes and pins atomically.
     * Per RFC Section 7.6 - CLEAR removes all notes and pins atomically.
     */
    public synchronized void clear() {
        notes.clear();
        pins.clear();
    }

    /**
     * Get count of notes
     */
    public synchronized int getNoteCount() {
        return notes.size();
    }

    /**
     * Get count of pins
     */
    public synchronized int getPinCount() {
        return pins.size();
    }
}
