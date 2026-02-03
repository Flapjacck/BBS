import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientBoard extends JFrame {
    private ClientConnection connection;
    private JTextArea outputArea;
    private JTextField postX, postY, postMsg, getColor, getX, getY, getRef, pinX, pinY;
    private JComboBox<String> postColor;
    private BoardCanvas boardCanvas;
    private static final String[] COLORS = { "yellow", "blue", "green", "pink", "orange", "purple", "white" };

    // Board state management
    private List<BoardCanvas.Note> boardNotes = new ArrayList<>();
    private List<int[]> boardPins = new ArrayList<>();

    public ClientBoard(ClientConnection connection) {
        this.connection = connection;
        setTitle("Bulletin Board Client - Visual Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1450, 700);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left panel: Controller
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Controller"));
        
        JPanel commands = new JPanel(new GridLayout(5, 1, 5, 5));

        // POST panel
        JPanel post = new JPanel(new FlowLayout(FlowLayout.LEFT));
        post.add(new JLabel("POST:"));
        post.add(new JLabel("X:"));
        post.add(postX = new JTextField("0", 3));
        post.add(new JLabel("Y:"));
        post.add(postY = new JTextField("0", 3));
        post.add(new JLabel("Color:"));
        post.add(postColor = new JComboBox<>(COLORS));
        post.add(new JLabel("Msg:"));
        post.add(postMsg = new JTextField("Hello", 10));
        JButton postBtn = new JButton("POST");
        postBtn.addActionListener(e -> executePost());
        post.add(postBtn);
        commands.add(post);

        // GET panel
        JPanel get = new JPanel(new FlowLayout(FlowLayout.LEFT));
        get.add(new JLabel("GET:"));
        get.add(new JLabel("Color:"));
        get.add(getColor = new JTextField(5));
        get.add(new JLabel("Contains:"));
        get.add(getX = new JTextField(3));
        get.add(getY = new JTextField(3));
        get.add(new JLabel("RefersTo:"));
        get.add(getRef = new JTextField(10));
        JButton getBtn = new JButton("GET");
        getBtn.addActionListener(e -> executeGet());
        get.add(getBtn);
        JButton getPinsBtn = new JButton("GET PINS");
        getPinsBtn.addActionListener(e -> executeCommand("GET PINS"));
        get.add(getPinsBtn);
        commands.add(get);

        // PIN panel
        JPanel pin = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pin.add(new JLabel("PIN/UNPIN:"));
        pin.add(new JLabel("X:"));
        pin.add(pinX = new JTextField("0", 3));
        pin.add(new JLabel("Y:"));
        pin.add(pinY = new JTextField("0", 3));
        JButton pinBtn = new JButton("PIN");
        pinBtn.addActionListener(e -> executeCommand("PIN " + pinX.getText() + " " + pinY.getText()));
        pin.add(pinBtn);
        JButton unpinBtn = new JButton("UNPIN");
        unpinBtn.addActionListener(e -> executeCommand("UNPIN " + pinX.getText() + " " + pinY.getText()));
        pin.add(unpinBtn);
        commands.add(pin);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton shakeBtn = new JButton("SHAKE");
        shakeBtn.addActionListener(e -> executeCommand("SHAKE"));
        actions.add(shakeBtn);
        JButton clearBtn = new JButton("CLEAR");
        clearBtn.addActionListener(e -> executeCommand("CLEAR"));
        actions.add(clearBtn);
        JButton disconnectBtn = new JButton("DISCONNECT");
        disconnectBtn.addActionListener(e -> {
            executeCommand("DISCONNECT");
            dispose();
        });
        actions.add(disconnectBtn);
        commands.add(actions);

        // Output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setText("=== Server Response Log ===\n");
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setPreferredSize(new Dimension(400, 200));

        leftPanel.add(commands, BorderLayout.NORTH);
        leftPanel.add(scroll, BorderLayout.CENTER);
        
        // Right panel: Visual Board
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Board (800x600)"));
        boardCanvas = new BoardCanvas();
        JScrollPane boardScroll = new JScrollPane(boardCanvas);
        rightPanel.add(boardScroll, BorderLayout.CENTER);

        main.add(leftPanel, BorderLayout.WEST);
        main.add(rightPanel, BorderLayout.CENTER);
        setContentPane(main);
        setVisible(true);
        
        // Initial board fetch
        SwingUtilities.invokeLater(() -> refreshBoardState());
    }

    private void executePost() {
        String cmd = String.format("POST %s %s %s %s",
                postX.getText().trim(), postY.getText().trim(),
                postColor.getSelectedItem(), postMsg.getText());
        executeCommand(cmd);
    }

    private void executeGet() {
        StringBuilder cmd = new StringBuilder("GET");
        if (!getColor.getText().isEmpty())
            cmd.append(" color=").append(getColor.getText());
        if (!getX.getText().isEmpty() && !getY.getText().isEmpty())
            cmd.append(" contains=").append(getX.getText()).append(" ").append(getY.getText());
        if (!getRef.getText().isEmpty())
            cmd.append(" refersTo=").append(getRef.getText());
        executeCommand(cmd.toString());
    }

    private void executeCommand(String command) {
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                return connection.sendCommand(command);
            }

            protected void done() {
                try {
                    String response = get();
                    outputArea.append("\n> " + command + "\n");
                    for (String line : response.split("\n")) {
                        if (line.startsWith("OK"))
                            outputArea.append("  ✓ " + line + "\n");
                        else if (line.startsWith("ERROR"))
                            outputArea.append("  ✗ " + line + "\n");
                        else
                            outputArea.append("    " + line + "\n");
                    }
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());

                    // Parse GET response and update board immediately
                    if (command.startsWith("GET") && !command.equals("GET PINS") && response.startsWith("OK")) {
                        // For regular GET, fetch pins immediately after to get complete state
                        String pinsResponse = connection.sendCommand("GET PINS");
                        parseAndMergeBoard(response, pinsResponse);
                    } else if (command.equals("GET PINS") && response.startsWith("OK")) {
                        // For GET PINS, merge with existing notes
                        parseAndMergePins(response);
                    } else if (response.startsWith("OK") && 
                               (command.startsWith("POST") || command.startsWith("PIN") || 
                                command.startsWith("UNPIN") || command.equals("SHAKE") || 
                                command.equals("CLEAR"))) {
                        // Auto-refresh board after state-changing commands
                        SwingUtilities.invokeLater(() -> {
                            refreshBoardState();
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.append("ERROR: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }

    /**
     * Refresh board state by executing GET and GET PINS commands together
     * Fetches both notes and pins, then merges them into board state
     */
    private void refreshBoardState() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                try {
                    // Fetch both notes and pins
                    String notesResponse = connection.sendCommand("GET");
                    String pinsResponse = connection.sendCommand("GET PINS");
                    
                    // Parse both responses and merge on EDT
                    SwingUtilities.invokeLater(() -> {
                        parseAndMergeBoard(notesResponse, pinsResponse);
                    });
                } catch (Exception ex) {
                    // Silently fail on auto-refresh
                }
                return null;
            }
        }.execute();
    }

    /**
     * Refresh pins by executing GET PINS command
     * Only updates pins, preserving existing notes
     */
    private void refreshPins() {
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                return connection.sendCommand("GET PINS");
            }

            protected void done() {
                try {
                    String response = get();
                    if (response != null && response.startsWith("OK")) {
                        System.out.println("Got pins response: " + response);
                        // Parse pins and merge with existing notes
                        parseAndMergePins(response);
                    }
                } catch (Exception ex) {
                    System.err.println("Error refreshing pins: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Parse both GET (notes) and GET PINS responses and merge them into board state
     * This is called when fetching complete board state
     */
    private void parseAndMergeBoard(String notesResponse, String pinsResponse) {
        try {
            List<BoardCanvas.Note> notes = new ArrayList<>();
            List<int[]> pins = new ArrayList<>();

            // Parse notes from GET response
            if (notesResponse != null && notesResponse.startsWith("OK")) {
                notes.addAll(parseNotes(notesResponse));
            }

            // Parse pins from GET PINS response
            if (pinsResponse != null && pinsResponse.startsWith("OK")) {
                pins.addAll(parsePins(pinsResponse));
            }

            // Update board canvas
            System.out.println("parseAndMergeBoard: updating with " + notes.size() + " notes and " + pins.size() + " pins");
            boardNotes = notes;
            boardPins = pins;
            boardCanvas.updateBoardState(notes, pins);
            boardCanvas.repaint();
            System.out.println("Canvas repaint triggered");
        } catch (Exception ex) {
            System.err.println("Error parsing merged board response: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Parse GET PINS response and merge with existing notes
     * This is called when only pins need to be refreshed
     */
    private void parseAndMergePins(String pinsResponse) {
        try {
            // Parse pins from response
            List<int[]> newPins = parsePins(pinsResponse);

            // Merge with existing notes
            System.out.println("parseAndMergePins: updating " + boardNotes.size() + " notes with " + newPins.size() + " pins");
            boardPins = newPins;
            boardCanvas.updateBoardState(boardNotes, boardPins);
            boardCanvas.repaint();
            System.out.println("Canvas repaint triggered");
        } catch (Exception ex) {
            System.err.println("Error parsing pins: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Parse notes from GET response
     * Format: OK <count>
     *         NOTE x y color message
     *         NOTE x y color message
     */
    private List<BoardCanvas.Note> parseNotes(String response) {
        List<BoardCanvas.Note> notes = new ArrayList<>();
        try {
            String[] lines = response.split("\n");
            if (lines.length < 1 || !lines[0].startsWith("OK")) return notes;

            int count = 0;
            try {
                String[] parts = lines[0].split(" ", 2);
                if (parts.length == 2) {
                    count = Integer.parseInt(parts[1]);
                }
            } catch (NumberFormatException e) {
                count = lines.length - 1;
            }

            for (int i = 1; i < Math.min(lines.length, count + 1); i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts.length >= 4 && parts[0].equals("NOTE")) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        String color = parts[3];
                        int msgStartIdx = line.indexOf(color) + color.length();
                        String message = msgStartIdx < line.length() ? line.substring(msgStartIdx).trim() : "";
                        notes.add(new BoardCanvas.Note(x, y, color, message));
                    } catch (NumberFormatException e) {
                        // Skip malformed line
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error parsing notes: " + ex.getMessage());
        }
        return notes;
    }

    /**
     * Parse pins from GET PINS response
     * Format: OK <count>
     *         PIN x y
     *         PIN x y
     */
    private List<int[]> parsePins(String response) {
        List<int[]> pins = new ArrayList<>();
        try {
            String[] lines = response.split("\n");
            if (lines.length < 1 || !lines[0].startsWith("OK")) return pins;

            int count = 0;
            try {
                String[] parts = lines[0].split(" ", 2);
                if (parts.length == 2) {
                    count = Integer.parseInt(parts[1]);
                }
            } catch (NumberFormatException e) {
                count = lines.length - 1;
            }

            for (int i = 1; i < Math.min(lines.length, count + 1); i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts.length == 3 && parts[0].equals("PIN")) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        pins.add(new int[]{x, y});
                    } catch (NumberFormatException e) {
                        // Skip malformed line
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error parsing pins: " + ex.getMessage());
        }
        return pins;
    }
}
