import javax.swing.*;
import java.awt.*;

// Simple bulletin board
public class Board extends JFrame {
    // Text area to display board posts
    private JTextArea postArea;

    public Board() {
        // Initialize the window
        setTitle("Bulletin Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title label
        JLabel titleLabel = new JLabel("Bulletin Board", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Board display panel
        JPanel boardPanel = new JPanel(new BorderLayout());
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Create text area for displaying posts
        postArea = new JTextArea();
        postArea.setEditable(false);
        postArea.setLineWrap(true);
        postArea.setWrapStyleWord(true);
        postArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        postArea.setText("Welcome to the Bulletin Board\n\n(No posts yet)");

        // Add scroll pane to allow scrolling through posts
        JScrollPane scrollPane = new JScrollPane(postArea);
        boardPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // Set content and display
        setContentPane(mainPanel);
        setVisible(true);
    }
}
