package client;

import javax.swing.*;
import java.awt.*;

public class Board extends JFrame {
    private ClientConnection connection;
    private JTextArea outputArea;
    private JTextField postX, postY, postMsg, getColor, getX, getY, getRef, pinX, pinY;
    private JComboBox<String> postColor;
    private static final String[] COLORS = { "yellow", "blue", "green", "pink", "orange", "purple", "white" };

    public Board(ClientConnection connection) {
        this.connection = connection;
        setTitle("BBS Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);

        JPanel main = new JPanel(new BorderLayout());
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
        JScrollPane scroll = new JScrollPane(outputArea);

        main.add(commands, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        setContentPane(main);
        setVisible(true);
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
                } catch (Exception ex) {
                    outputArea.append("ERROR: " + ex.getMessage() + "\n");
                }
            }
        }.execute();
    }
}
