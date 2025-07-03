package graphics;

import consts.GameConsts;
import java.awt.GridLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import logic.GameMonitor;

public class ChessBoard extends JPanel {
    private Square[][] _board;
    private int _srcRow = -1, _srcCol = -1;
    private GameMonitor _listener;
    private boolean _isFlipped;
    
    public ChessBoard(GameMonitor gm){
        setLayout(new GridLayout(GameConsts.NUM_OF_ROWS, GameConsts.NUM_OF_ROWS));
        createBoard();
        _listener = gm;
    }

    private void createBoard() {
        _board = new Square[GameConsts.NUM_OF_ROWS][GameConsts.NUM_OF_ROWS];
    
        for (int i = 0; i < GameConsts.NUM_OF_ROWS; i++) {
            for (int j = 0; j < GameConsts.NUM_OF_ROWS; j++) {
                _board[i][j] = new Square(i, j);
                _board[i][j].setListener(this); // Listen to each square
                setupInitialPieces(i, j); // Setup up the pieces to their initial squares
            }
        }

        for (int i = GameConsts.NUM_OF_ROWS - 1; i >= 0; i--) {
            for (int j = 0; j < GameConsts.NUM_OF_ROWS; j++) {
                add(_board[i][j]);
            }
        }
    }
    
    private void setupInitialPieces(int row, int col) {
        if (row == 1) {
            _board[row][col].setPiece("wp"); // White pawns
        } else if (row == 6) {
            _board[row][col].setPiece("bp"); // Black pawns
        } else if (row == 0) {
            // White back row
            switch (col) {
                case 0, 7 -> _board[row][col].setPiece("wr"); // Rooks
                case 1, 6 -> _board[row][col].setPiece("wn"); // Knights
                case 2, 5 -> _board[row][col].setPiece("wb"); // Bishops
                case 3 -> _board[row][col].setPiece("wq"); // Queen
                case 4 -> _board[row][col].setPiece("wk"); // King
            }
        } else if (row == 7) {
            // Black back row
            switch (col) {
                case 0, 7 -> _board[row][col].setPiece("br"); // Rooks
                case 1, 6 -> _board[row][col].setPiece("bn"); // Knights
                case 2, 5 -> _board[row][col].setPiece("bb"); // Bishops
                case 3 -> _board[row][col].setPiece("bq"); // Queen
                case 4 -> _board[row][col].setPiece("bk"); // King
            }
        }
    }
    
    public void setBoards(long[][] boards) {
        // Clear board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                _board[row][col].removePiece();
            }
        }

        // Draw white pieces
        for (int type = 0; type < 6; type++) {
            long bitboard = boards[0][type];
            for (int i = 0; i < 64; i++) {
                if (((bitboard >> i) & 1) != 0) {
                    int row = i / 8;
                    int col = i % 8;
                    String code = "w" + GameConsts.SYMBOLS[type];
                    _board[row][col].setPiece(code);
                }
            }
        }

        // Draw black pieces
        for (int type = 0; type < 6; type++) {
            long bitboard = boards[1][type];
            for (int i = 0; i < 64; i++) {
                if (((bitboard >> i) & 1) != 0) {
                    int row = i / 8;
                    int col = i % 8;
                    String code = "b" + GameConsts.SYMBOLS[type];
                    _board[row][col].setPiece(code);
                }
            }
        }

        revalidate();
        repaint();
    }

    public void flipBoard() {
        _isFlipped = !_isFlipped;
        removeAll();
    
        if (_isFlipped) {
            // Bottom to top, right to left
            for (int i = 0; i < GameConsts.NUM_OF_ROWS; i++) {
                for (int j = GameConsts.NUM_OF_ROWS - 1; j >= 0; j--) {
                    add(_board[i][j]);
                }
            }
        } else {
            // Top to bottom, left to right (default view)
            for (int i = GameConsts.NUM_OF_ROWS - 1; i >= 0; i--) {
                for (int j = 0; j < GameConsts.NUM_OF_ROWS; j++) {
                    add(_board[i][j]);
                }
            }
        }
    
        revalidate();
        repaint();
    }
    
    public void squareSelected(int row, int col) {
        String clickedPiece = _board[row][col].getPiece();
        char currentPlayer = _listener.isWhiteTurn() ? 'w' : 'b';
        
        clearHighlights();
        
        // No piece selected yet
        if (_srcRow == -1 || _srcCol == -1) {
            if (clickedPiece != null && clickedPiece.charAt(0) == currentPlayer) {
                _srcRow = row;
                _srcCol = col;
                _listener.showlegalMoves(clickedPiece, row, col);
            }
            return;
        }

        // Clicked the same square
        if (_srcRow == row && _srcCol == col) {
            _srcRow = -1;
            _srcCol = -1;
            return;
        }
    
        String srcPiece = _board[_srcRow][_srcCol].getPiece();
    
        // If clicked another own piece
        if (clickedPiece != null && clickedPiece.charAt(0) == srcPiece.charAt(0)) {
            _srcRow = row;
            _srcCol = col;
            return;
        }
    
        // Try to move if dest is empty or enemy piece
        if (srcPiece != null && (clickedPiece == null || clickedPiece.charAt(0) != srcPiece.charAt(0))) {
            _listener.handleMove(convertToMoveString(_srcRow, _srcCol, row, col));
        }
    
        // Reset selection - move occured / failed
        _srcRow = -1;
        _srcCol = -1;
    }
    
    public void clearHighlights() {
        for (int i = 0; i < GameConsts.NUM_OF_ROWS; i++) {
            for (int j = 0; j < GameConsts.NUM_OF_ROWS; j++) {
                _board[i][j].setIsHighlighted(false, false);
            }
        }
    }
    
    public static String convertToMoveString(int srcRow, int srcCol, int destRow, int destCol) {
        char srcFile = (char) ('a' + srcCol);
        int srcRank = srcRow + 1;
        char destFile = (char) ('a' + destCol);
        int destRank = destRow + 1;
    
        return "" + srcFile + srcRank + destFile + destRank;
    }
    
    private int[] convertStringToMove(String move) {
        int srcCol = move.charAt(0) - 'a';
        int srcRow = Character.getNumericValue(move.charAt(1)) - 1;
        int destCol = move.charAt(2) - 'a';
        int destRow = Character.getNumericValue(move.charAt(3)) - 1;
        return new int[]{srcRow, srcCol, destRow, destCol};
    }
        
    /*
     The function copy the piece from source square to a dest square 
     and then remove the piece from the source square.
     Finally the source and dest squares are set to -1.
    */
    public void movePiece(String move) {
        int[] m = convertStringToMove(move);
        int srcRow = m[0], srcCol = m[1], destRow = m[2], destCol = m[3];
    
        _board[destRow][destCol].setPiece(_board[srcRow][srcCol].getPiece());
        _board[srcRow][srcCol].removePiece();
    }

    public void highlightSquare(int row, int col, boolean check) {
        _board[row][col].setIsHighlighted(true, check);
    }

    public void onEnPassant(int row, int col) {
        _board[row][col].removePiece();
    }

    public String pawnPromotionDialog(boolean isWhite) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose piece to promote to:",
            "Pawn Promotion",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]);

        char colorChar = isWhite ? 'w' : 'b';
        return switch (choice) {
            case 1 -> colorChar + "r";
            case 2 -> colorChar + "b";
            case 3 -> colorChar + "n";
            default -> colorChar + "q";
        };
    }

    public void promotePiece(int srcRow, int srcCol, int destRow, int destCol, String piece){
        _board[srcRow][srcCol].removePiece();
        _board[destRow][destCol].setPiece(piece);
    }
    
    public void gameEnded(boolean whiteWon, boolean draw) {
        String message;
        if (draw) {
            message = "It's a draw!";
        } else {
            message = whiteWon ? "White won!" : "Black won!";
        }
    
        JOptionPane.showMessageDialog(
            this,
            message,
            "Game Over",
            JOptionPane.INFORMATION_MESSAGE
        );
    
        SwingUtilities.getWindowAncestor(this).dispose();
        new OpeningScreen();
    }    
}