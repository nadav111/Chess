package graphics;

import consts.GameConsts;
import java.awt.*;
import javax.swing.*;
import logic.BitBoard;

public class TestingFrame extends JFrame {

    public TestingFrame() {
        setTitle("Test EndingTree");
        setSize(300, 180);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(60, 63, 65));

        JButton testButton = new JButton("Test EndingTree");
        testButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        testButton.setBackground(new Color(90, 90, 90));
        testButton.setForeground(Color.WHITE);
        testButton.setFocusPainted(false);
        testButton.addActionListener(e -> runEndingTreeTest());

        add(testButton, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void runEndingTreeTest() {
        GameScreen gs = new GameScreen("tester1", "tester2", true, false, 3600); // Bot must be black
        long[][] boards = new long[4][];

        // 1. White pieces
        boards[0] = new long[6];
        boards[0][GameConsts.KING]   = 1L << BitBoard.squareToIndex("g1");
        boards[0][GameConsts.ROOK]   = 1L << BitBoard.squareToIndex("f1");
        boards[0][GameConsts.BISHOP] = 1L << BitBoard.squareToIndex("c4");
        boards[0][GameConsts.KNIGHT] = 1L << BitBoard.squareToIndex("e5");
        boards[0][GameConsts.PAWN]   = 1L << BitBoard.squareToIndex("g6");

        // 2. Black pieces
        boards[1] = new long[6];
        boards[1][GameConsts.KING]   = 1L << BitBoard.squareToIndex("g8");
        boards[1][GameConsts.QUEEN]  = 1L << BitBoard.squareToIndex("a5");
        boards[1][GameConsts.ROOK]   = 1L << BitBoard.squareToIndex("a8");
        boards[1][GameConsts.BISHOP] = 1L << BitBoard.squareToIndex("f8");
        boards[1][GameConsts.KNIGHT] = 1L << BitBoard.squareToIndex("c6");
        boards[1][GameConsts.PAWN]   = (1L << BitBoard.squareToIndex("f7")) | 
                                    (1L << BitBoard.squareToIndex("h6")); // 2 pawns

        // 3. En passant
        boards[2] = new long[] { 0L };

        // 4. Castling rights
        boards[3] = new long[] { 0, 0, 0, 0 }; // [wK, wQ, bK, bQ]

        gs.setBoard(boards);
    }
}
