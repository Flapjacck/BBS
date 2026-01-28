/**
 * Note represents a single note on the bulletin board.
 * 
 * Per RFC Section 4.3 - Note Properties:
 * - Upper-left coordinate (x, y) defines position
 * - Fixed width and height (set at server config)
 * - Color selected from server's predefined palette
 * - Free-form text content (message)
 * - Zero or more associated pins (tracked separately in Board)
 * 
 * Per RFC Section 4.1 - Coordinate System:
 * - Origin (0,0) at upper-left corner
 * - x increases rightward, y increases downward
 * - All coordinates are non-negative integers
 */
public class Note {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String color;
    private final String message;

    /**
     * Create a new note with specified properties.
     * 
     * @param x       Upper-left x coordinate
     * @param y       Upper-left y coordinate
     * @param width   Note width (fixed by server config)
     * @param height  Note height (fixed by server config)
     * @param color   Color from server's palette
     * @param message Free-form text content
     */
    public Note(int x, int y, int width, int height, String color, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.message = message;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getColor() {
        return color;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Check if a coordinate point lies within this note's bounds.
     * Per RFC Section 7.3 - PIN applies to all notes containing the coordinate.
     * 
     * @param px X coordinate to check
     * @param py Y coordinate to check
     * @return true if point (px, py) is inside this note
     */
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    /**
     * Check if this note completely overlaps another note.
     * Per RFC Section 4.3 - Partial overlaps permitted; exact overlaps rejected.
     * Complete overlap means identical rectangular regions.
     * 
     * @param other The other note to compare against
     * @return true if both notes occupy identical regions
     */
    public boolean overlapsCompletely(Note other) {
        return this.x == other.x && this.y == other.y
                && this.width == other.width && this.height == other.height;
    }

    /**
     * Check if this note lies fully within board boundaries.
     * Per RFC Section 4.2 - Notes must lie fully inside the board.
     * 
     * @param boardWidth  Board width
     * @param boardHeight Board height
     * @return true if note is fully within bounds
     */
    public boolean isWithinBounds(int boardWidth, int boardHeight) {
        return x >= 0 && y >= 0
                && (x + width) <= boardWidth
                && (y + height) <= boardHeight;
    }

    /**
     * Format note for GET response per RFC Appendix A.2.
     * Format: NOTE <x> <y> <color> <message>
     */
    @Override
    public String toString() {
        return "NOTE " + x + " " + y + " " + color + " " + message;
    }
}
