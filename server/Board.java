import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Board manages shared bulletin board state with thread-safe operations.
 * Thread-per-client model; all modifying ops (POST, PIN, UNPIN, SHAKE, CLEAR)
 * are synchronized.
 * Width/height fixed at startup; notes must lie fully inside board.
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

    public Board(int width, int height, int noteWidth, int noteHeight, Set<String> colors) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColors = new HashSet<>(colors);
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();
    }

    // Getters
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

    public boolean isValidColor(String color) {
        return validColors.contains(color.toLowerCase());
    }

    /**
     * Add note to board (POST). Thread-safe. Returns null on success, error string
     * on failure.
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
     * Get all notes with optional filters (GET). All filters use AND logic.
     * Thread-safe.
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

    /** Get all pins (GET PINS). Thread-safe. Returns list of [x, y] coordinates. */
    public synchronized List<int[]> getPins() {
        return new ArrayList<>(pins);
    }

    /**
     * Add pin at coordinate (PIN). Pin must be within at least one note.
     * Only one pin allowed per coordinate (no duplicates).
     * Thread-safe.
     */
    public synchronized String addPin(int x, int y) {
        // Validate coordinates are non-negative per RFC Section 4.1
        if (x < 0 || y < 0) {
            return "OUT_OF_BOUNDS";
        }

        // Check if pin already exists at this coordinate
        for (int[] existingPin : pins) {
            if (existingPin[0] == x && existingPin[1] == y) {
                return "PIN_ALREADY_EXISTS";
            }
        }

        // Check if pin is within at least one note per RFC Section 9.1
        boolean withinNote = false;
        for (Note note : notes) {
            if (note.contains(x, y)) {
                withinNote = true;
                break;
            }
        }

        if (!withinNote) {
            return "NO_NOTE_AT_COORDINATE";
        }

        // Add pin to the list
        pins.add(new int[] { x, y });
        return null; // Success
    }

    /** Remove pin at coordinate (UNPIN). Thread-safe. */
    public synchronized String removePin(int x, int y) {
        // Find and remove the pin
        for (int i = 0; i < pins.size(); i++) {
            int[] pin = pins.get(i);
            if (pin[0] == x && pin[1] == y) {
                pins.remove(i);
                return null; // Success
            }
        }

        // Pin not found per RFC Section 9.1
        return "PIN_NOT_FOUND";
    }

    /**
     * Remove all unpinned notes (SHAKE). A note is pinned if any pin exists within
     * bounds. Thread-safe.
     */
    public synchronized void removeUnpinnedNotes() {
        List<Note> pinnedNotes = new ArrayList<>();

        // Check each note to see if it has any pins
        for (Note note : notes) {
            boolean isPinned = false;
            for (int[] pin : pins) {
                if (note.contains(pin[0], pin[1])) {
                    isPinned = true;
                    break;
                }
            }
            if (isPinned) {
                pinnedNotes.add(note);
            }
        }

        // Replace notes list with only pinned notes
        notes.clear();
        notes.addAll(pinnedNotes);
    }

    /** Clear all notes and pins atomically (CLEAR). Thread-safe. */
    public synchronized void clear() {
        notes.clear();
        pins.clear();
    }

    public synchronized int getNoteCount() {
        return notes.size();
    }

    public synchronized int getPinCount() {
        return pins.size();
    }
}
