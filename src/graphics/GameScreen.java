package graphics;
import consts.GameConsts;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import logic.GameMonitor;

public class GameScreen extends JFrame implements ActionListener {

    private String _whiteName;
    private String _blackName;
    private boolean _vsAI;
    private boolean _isBotWhite;

    private JLabel _lbWhiteTimer;
    private JLabel _lbBlackTimer;
    private Timer _t;
    private int _whiteTime = 300; // 5 min
    private int _blackTime = 300;
    private int _time;
    private GameMonitor _gm;

    public GameScreen(String whiteName, String blackName, boolean vsAI, boolean isBotWhite, int time) {
        setTitle("Chess Game");
        setSize(1000, 800);
        getContentPane().setBackground(GameConsts.BG_THEME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        _vsAI = vsAI;
        _isBotWhite = isBotWhite;
        
        _gm = new GameMonitor(vsAI, isBotWhite);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JPanel bottomPanel = new JPanel(new FlowLayout());

        _whiteName = whiteName;
        _blackName = blackName;

        JButton flipButton = new JButton("Flip Board");
        JButton restartButton = new JButton("Restart Game");
        JButton homeButton = new JButton("Home");

        styleButton(flipButton);
        styleButton(restartButton);
        styleButton(homeButton);

        flipButton.addActionListener(e -> _gm.getGraphicBoard().flipBoard());
        restartButton.addActionListener(e -> restartGame());
        homeButton.addActionListener(e -> goHome());

        topPanel.setBackground(GameConsts.BG_THEME);
        bottomPanel.setBackground(GameConsts.BG_THEME);


        bottomPanel.add(restartButton);
        bottomPanel.add(homeButton);

        add(topPanel, BorderLayout.NORTH);
        add(_gm.getGraphicBoard(), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // handle timers
        _time = time;
        _whiteTime = time;
        _blackTime = time;
        
        _t = new Timer( 1000, this);
        _t.start();
        
        _lbWhiteTimer = new JLabel();
        _lbBlackTimer = new JLabel();

        styleLabel(_lbWhiteTimer);
        styleLabel(_lbBlackTimer);
        
        updateLabel(_lbWhiteTimer, _whiteTime, _whiteName);
        updateLabel(_lbBlackTimer, _blackTime, _blackName);

        // add timers
        topPanel.add(_lbWhiteTimer);
        topPanel.add(flipButton);
        topPanel.add(_lbBlackTimer);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Called each time the timer is on
    @Override
    public void actionPerformed(ActionEvent e) {
        if (_gm.isWhiteTurn()) {
            if (_whiteTime > 0) {
                _whiteTime--;
                updateLabel(_lbWhiteTimer, _whiteTime, _whiteName);
            }
            if (_whiteTime == 0) {
                _t.stop();
                JOptionPane.showMessageDialog(this, _whiteName + " ran out of time! " + _blackName + " wins.");
            }
        } else {
            if (_blackTime > 0) {
                _blackTime--;
                updateLabel(_lbBlackTimer, _blackTime, _blackName);
            }
            if (_blackTime == 0) {
                _t.stop();
                JOptionPane.showMessageDialog(this, _blackName + " ran out of time! " + _whiteName + " wins.");
            }
        }
    }
    
    private void updateLabel(JLabel label, int time, String player) {
        int min = time / 60;
        int sec = time % 60;
        label.setText(player + ": " + String.format("%02d:%02d", min, sec));
    }

    private void restartGame() {
        _t.stop();
        dispose();
        new GameScreen(_whiteName, _blackName, _vsAI, _isBotWhite, _time);
    }

    private void styleButton(JButton button) {
        button.setBackground(GameConsts.BOARD_BLACK);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
    }

    private void goHome() {
        new OpeningScreen();
        dispose();
    }

    public void setBoard(long[][] boards) {
        _gm.setBoard(boards);
    }
}
