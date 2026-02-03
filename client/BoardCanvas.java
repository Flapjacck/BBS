import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Visual representation of the bulletin board with notes and pins.
 * Board dimensions: 800x600, each note: 100x100 (RFC Section 4.3)
 */
public class BoardCanvas extends JPanel {
    public static final int BOARD_WIDTH = 800;
    public static final int BOARD_HEIGHT = 600;
    public static final int NOTE_WIDTH = 100;
    public static final int NOTE_HEIGHT = 100;

    private static final Map<String, Color> COLOR_MAP = Map.ofEntries(
            Map.entry("yellow", new Color(255, 255, 0)),
            Map.entry("blue", new Color(0, 0, 255)),
            Map.entry("green", new Color(0, 128, 0)),
            Map.entry("pink", new Color(255, 192, 203)),
            Map.entry("orange", new Color(255, 165, 0)),
            Map.entry("purple", new Color(128, 0, 128)),
            Map.entry("white", new Color(255, 255, 255))
    );

    private List<Note> notes = new ArrayList<>();
    private List<int[]> pins = new ArrayList<>();

    public static class Note {
        public int x, y;
        public String color;
        public String message;

        public Note(int x, int y, String color, String message) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.message = message;
        }
    }

    public BoardCanvas() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void updateBoardState(List<Note> notes, List<int[]> pins) {
        this.notes = new ArrayList<>(notes);
        this.pins = new ArrayList<>(pins);
        repaint();
    }

    public void clearBoard() {
        this.notes.clear();
        this.pins.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);
        for (Note note : notes) {
            drawNote(g2d, note);
        }

        List<int[]> deduplicatedPins = getDeduplicatedPins();
        for (int[] pin : deduplicatedPins) {
            drawPin(g2d, pin[0], pin[1]);
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(0, 0, BOARD_WIDTH - 1, BOARD_HEIGHT - 1);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));

        for (int x = 0; x <= BOARD_WIDTH; x += 50) {
            g2d.drawLine(x, 0, x, BOARD_HEIGHT);
        }

        for (int y = 0; y <= BOARD_HEIGHT; y += 50) {
            g2d.drawLine(0, y, BOARD_WIDTH, y);
        }
    }

    private List<int[]> getDeduplicatedPins() {
        List<int[]> deduplicatedPins = new ArrayList<>();
        List<Note> pinnedNotes = new ArrayList<>();

        for (int[] pin : pins) {
            int pinX = pin[0];
            int pinY = pin[1];
            
            Note pinNoteRef = null;
            for (Note note : notes) {
                if (note.x == pinX && note.y == pinY) {
                    pinNoteRef = note;
                    break;
                }
            }
            
            boolean noteAlreadyPinned = false;
            if (pinNoteRef != null) {
                for (Note pinnedNote : pinnedNotes) {
                    if (pinnedNote.x == pinNoteRef.x && pinnedNote.y == pinNoteRef.y) {
                        noteAlreadyPinned = true;
                        break;
                    }
                }
            } else {
                for (Note note : notes) {
                    if (pinX >= note.x && pinX < note.x + NOTE_WIDTH &&
                        pinY >= note.y && pinY < note.y + NOTE_HEIGHT) {
                        pinNoteRef = note;
                        for (Note pinnedNote : pinnedNotes) {
                            if (pinnedNote.x == note.x && pinnedNote.y == note.y) {
                                noteAlreadyPinned = true;
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (!noteAlreadyPinned && pinNoteRef != null) {
                deduplicatedPins.add(pin);
                pinnedNotes.add(pinNoteRef);
            }
        }
        
        return deduplicatedPins;
    }

    private void drawNote(Graphics2D g2d, Note note) {
        Color noteColor = COLOR_MAP.getOrDefault(note.color.toLowerCase(), Color.LIGHT_GRAY);

        g2d.setColor(noteColor);
        g2d.fillRect(note.x, note.y, NOTE_WIDTH, NOTE_HEIGHT);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(note.x, note.y, NOTE_WIDTH, NOTE_HEIGHT);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        String text = note.message;
        if (text.length() > 15) {
            text = text.substring(0, 12) + "...";
        }

        int textX = note.x + 5;
        int textY = note.y + NOTE_HEIGHT / 2;
        g2d.drawString(text, textX, textY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        String coords = "(" + note.x + "," + note.y + ")";
        g2d.drawString(coords, note.x + 5, note.y + NOTE_HEIGHT - 5);
    }

    private void drawPin(Graphics2D g2d, int x, int y) {
        int pinX = x + 50;
        int pinY = y + 5;
        
        int pinRadius = 5;
        int glowRadius = 9;
        
        g2d.setColor(new Color(255, 100, 100, 100));
        g2d.fillOval(pinX - glowRadius, pinY - glowRadius, glowRadius * 2, glowRadius * 2);
        
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(pinX - pinRadius + 1, pinY - pinRadius + 1, pinRadius * 2, pinRadius * 2);
        
        g2d.setColor(new Color(255, 50, 50));
        g2d.fillOval(pinX - pinRadius, pinY - pinRadius, pinRadius * 2, pinRadius * 2);
        
        g2d.setColor(new Color(255, 150, 150, 200));
        g2d.fillOval(pinX - pinRadius + 1, pinY - pinRadius + 1, 2, 2);
        
        g2d.setColor(new Color(139, 0, 0));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(pinX - pinRadius, pinY - pinRadius, pinRadius * 2, pinRadius * 2);
        
        g2d.setColor(new Color(200, 0, 0));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(pinX, pinY + pinRadius, pinX, pinY + pinRadius + 5);
    }
}
