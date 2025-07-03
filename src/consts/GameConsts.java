package consts;

import java.awt.Color;

public class GameConsts {
    

    //================================= LOGIC =================================
    public static final int NUM_OF_ROWS = 8; // O(n)
    public static final int NUM_OF_SQUARES = 64; // O(n^2)
    public static final int KING = 0, QUEEN = 1, ROOK = 2, BISHOP = 3, KNIGHT = 4, PAWN = 5;
    public static final String[] SYMBOLS = {"k", "q", "r", "b", "n", "p"};
    public static final int[] PIECE_VALS = {100, 9, 5, 3, 3, 1};

    // Masks:
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_H = 0x8080808080808080L;
    public static final long RANK_1 = 0x00000000000000FFL;
    public static final long RANK_8 = 0xFF00000000000000L;

    public static final long RANK_2 = 0x000000000000FF00L;
    public static final long RANK_7 = 0x00FF000000000000L;

    // Masks to prevent wraparounds - Knight
    public static final long DDR = 0x0000FEFEFEFEFEFEL; // Down-Down-Right
    public static final long DDL = 0x00007F7F7F7F7F7FL; // Down-Down-Left
    public static final long UUR = 0xFEFEFEFEFEFE0000L; // Up-Up-Right
    public static final long UUL = 0x7F7F7F7F7F7F0000L; // Up-Up-Left
    
    public static final long RRU = 0xFCFCFCFCFCFCFCFCL; // Right-Right-Up
    public static final long RRD = 0xFCFCFCFCFCFCFCFCL; // Right-Right-Down
    public static final long LLU = 0x3F3F3F3F3F3F3F3FL; // Left-Left-Up
    public static final long LLD = 0x3F3F3F3F3F3F3F3FL; // Left-Left-Down

    //================================= GRAPHICS =================================
    public final static Color BOARD_BLACK = new Color(70, 70, 70);
    public final static Color BOARD_WHITE= new Color(20, 20, 20);
    public final static Color BG_THEME = new Color(60, 63, 65);

    public static final String INSTRUCTIONS = "Instructions:\n" +
        "Chess is a two-player strategy game played on an 8x8 square board.\n" +
        "Each player starts with 16 pieces: one king, one queen, two rooks, two knights, two bishops, and eight pawns.\n\n" +
        "Objective: Checkmate the opponent's king, meaning the king is under attack and cannot escape.\n\n" +
        "Piece Movements:\n" +
        "- Rooks move horizontally or vertically\n" +
        "- Knights move in an 'L' shape\n" +
        "- Bishops move diagonally\n" +
        "- Queens combine the moves of rooks and bishops\n" +
        "- Pawns move forward one square, capturing diagonally\n\n" +
        "Special Moves:\n" +
        "- Castling: A move involving the king and a rook\n" +
        "- En Passant: A special pawn capture\n" +
        "- Pawn Promotion: Pawns can promote to any other piece upon reaching the back rank.\n\n" +
        "The game ends when one player checkmates the other, or the game is drawn.";
}