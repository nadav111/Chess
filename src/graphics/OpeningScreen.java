package graphics;
import consts.GameConsts;
import java.awt.*;
import javax.swing.*;

public class OpeningScreen extends JFrame {
    
    private String _whiteName;
    private String _blackName;
    private boolean _isPlayerWhite;

    public OpeningScreen() {
        setTitle("Chess");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(GameConsts.BG_THEME);

        new TestingFrame();
        // Title
        JLabel titleLabel = new JLabel("Chess", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setBackground(GameConsts.BG_THEME);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 60));

        JButton startButton = createButton("Start Game", e -> startGame());
        JButton instructionsButton = createButton("Instructions", e -> showInstructions());
        JButton settingsButton = createButton("Settings", e -> openSettings());
        JButton exitButton = createButton("Exit", e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(instructionsButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(90, 90, 90));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 120, 120), 1),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.addActionListener(action);
        return button;
    }
    
    private void startGame() {
        int mode = chooseGameMode();
        if (mode == JOptionPane.CLOSED_OPTION) return;

        boolean vsAI = (mode == 1);
        boolean success = vsAI ? collectAiData() : collectPvpData();
        if (!success) return;

        int minutes = collectTimeControl();
        if (minutes == -1) return;

        new GameScreen(_whiteName, _blackName, vsAI, !_isPlayerWhite, minutes);
        dispose();
    }

            
    private int chooseGameMode() {
        String[] options = {"Player vs Player", "Player vs AI"};
        return JOptionPane.showOptionDialog(
            null,
            "Choose game mode:",
            "Game Mode",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );
    }

    private boolean collectAiData() {
        String[] colorOptions = {"Play as White", "Play as Black"};
        int colorChoice = JOptionPane.showOptionDialog(
            null,
            "Choose your color:",
            "Color Selection",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            colorOptions,
            colorOptions[0]
        );

        if (colorChoice == JOptionPane.CLOSED_OPTION) return false;

        _isPlayerWhite = (colorChoice == 0);
        _whiteName = _isPlayerWhite ? "YOU" : "AI";
        _blackName = _isPlayerWhite ? "AI" : "YOU";
        return true;
    }

    private boolean collectPvpData() {
        JTextField whiteField = new JTextField("White");
        JTextField blackField = new JTextField("Black");

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("White player name:"));
        panel.add(whiteField);
        panel.add(new JLabel("Black player name:"));
        panel.add(blackField);

        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Enter Player Names",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return false;

        _whiteName = whiteField.getText().trim();
        _blackName = blackField.getText().trim();

        if (_whiteName.isEmpty()) _whiteName = "White";
        if (_blackName.isEmpty()) _blackName = "Black";
        _isPlayerWhite = true;

        return true;
    }
    
    private int collectTimeControl() {
        String[] timeOptions = {
            "Blitz (5 minutes)", 
            "Rapid (15 minutes)", 
            "Classical (30 minutes)"
        };

        int choice = JOptionPane.showOptionDialog(
            null,
            "Select time control:",
            "Time Control",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            timeOptions,
            timeOptions[0]
        );

        return switch (choice) {
            case 0 -> 5*60;
            case 1 -> 15*60;
            case 2 -> 30*60;
            default -> -1; // Closed or invalid
        };
    }


    
    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
            GameConsts.INSTRUCTIONS,
            "Instructions",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSettings() {
        JOptionPane.showMessageDialog(this,
            "Nah.",
            "Settings",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
