package logic;

import consts.GameConsts;

public class BitBoard {
    // =========================== PIECE BOARDS ===========================
    private long[] _whitePieces;
    private long[] _blackPieces;

    // =========================== GAME STATE ===========================
    private long _enPassentMove = 0L;
    
     // king's side white, queen's side white, king's side black, queen's side black
    private boolean _canCastle[] = {true,true,true,true};
    
    // Game Monitor - bridge to the graphics
    private GameMonitor _listener;

    // =========================== CONSTRUCTOR ===========================
    public BitBoard(GameMonitor gm) {
        _listener = gm;
        
        // Board's Creation
        _whitePieces = new long[6];
        _blackPieces = new long[6];

        setUpTheBoard();
    }

    private void setUpTheBoard() {
        _whitePieces[GameConsts.KING] = 0x0000000000000010L;
        _whitePieces[GameConsts.QUEEN] = 0x0000000000000008L;
        _whitePieces[GameConsts.ROOK] = 0x0000000000000081L;
        _whitePieces[GameConsts.BISHOP] = 0x0000000000000024L;
        _whitePieces[GameConsts.KNIGHT] = 0x0000000000000042L;
        _whitePieces[GameConsts.PAWN] = 0x000000000000FF00L;

        _blackPieces[GameConsts.KING] = 0x1000000000000000L;
        _blackPieces[GameConsts.QUEEN] = 0x0800000000000000L;
        _blackPieces[GameConsts.ROOK] = 0x8100000000000000L;
        _blackPieces[GameConsts.BISHOP] = 0x2400000000000000L;
        _blackPieces[GameConsts.KNIGHT] = 0x4200000000000000L;
        _blackPieces[GameConsts.PAWN] = 0x00FF000000000000L;
    }

    // ================ MAKE MOVE ================

    // Gets a move string ("e2e4") and make this move on the bitboard.
    public boolean makeMove(String move, Boolean isWhite) {
        // pointers to each pieces of the player
        long[] playerPieces, opponentPieces; 

        // convert to index
        int fromIndex = squareToIndex(move.substring(0, 2));
        int toIndex = squareToIndex(move.substring(2, 4));
    
        // create the board of the move
        long fromBit = 1L << fromIndex;
        long toBit = 1L << toIndex;
    
        if (isWhite) 
        {
            playerPieces = _whitePieces;
            opponentPieces = _blackPieces;
        }
        else 
        {
            playerPieces = _blackPieces;
            opponentPieces = _whitePieces;
        }
        long[][] boards = new long[][] {
            (isWhite?_whitePieces:_blackPieces).clone(),
            (isWhite?_blackPieces:_whitePieces).clone(),
            new long[] { _enPassentMove },
            new long[] {
                _canCastle[0] ? 1L : 0L,
                _canCastle[1] ? 1L : 0L,
                _canCastle[2] ? 1L : 0L,
                _canCastle[3] ? 1L : 0L
            }        
        };

        // what piece had been moved
        int pieceIndex = -1;
        boolean found = false;
        for (int i = 0; i < 6 && !found; i++) {
            if ((playerPieces[i] & fromBit) != 0) {
                pieceIndex = i;
                found = true;
            }
        }

        // if no piece has moved return false
        if (pieceIndex == -1) {
            System.out.println("Invalid move: No piece found.");
            return false;
        }
        
        // get the legal moves of the piece that move
        long legalMoves = Moves.getLegalMoves(boards, pieceIndex, fromIndex, isWhite);
        
        // if the dest square is not on the legal moves of the piece return false
        if ((legalMoves & toBit) == 0) {
            System.out.println("Illegal move.");
            return false;
        }
        
        // if the piece that moved is a pawn, handle en passant
        if (pieceIndex == GameConsts.PAWN) {
            captureEnPassant(isWhite, toBit, opponentPieces);
            activateEnPassant(isWhite, fromBit, toBit);
        } else {
            _enPassentMove = 0L; // Reset if not a pawn move
        }

        // if the piece that moved is a king, handle castling
        if (pieceIndex == GameConsts.KING) {
            if (isWhite) {
                _canCastle[0] = _canCastle[1] = false;
            } else {
                _canCastle[2] = _canCastle[3] = false;
            }
            handleCastling(move);
        }
        
        // if the piece that moved is a rook, disable castling
        if (pieceIndex == GameConsts.ROOK) {
            handleRookCastleRights(fromBit, isWhite);
        }
    
        // stimulate the move and check if the move is illegal
        if (!Moves.isMoveLegal(boards, pieceIndex, fromBit, toBit, isWhite)) {
            System.out.println("Move is illegal: leaves king in check.");
            return false;
        }
        
        playerPieces[pieceIndex] &= ~fromBit;
        playerPieces[pieceIndex] |= toBit;

        // Update the opponent pieces according to the move (If a piece is captured)
        for (int i = 0; i < 6; i++) {
            opponentPieces[i] &= ~toBit;
        }

        // first make the move, after that promote the pawn
        if (pieceIndex == GameConsts.PAWN && checkForPawnPromotion(isWhite, toBit)) {
            promotePieceBitBoard(_listener.pawnPromotion(fromBit, toBit), toBit);
        }
        
        System.out.println("Move: " + move);
        return true;
    }
   
    // ==================== HANDLE PAWN MOVES ====================

    // EN PASSASNT:

    // If the dest square is the en passant square, capture the pawn behind it
    private void captureEnPassant(boolean isWhite, long toBit, long[] opponentPieces) {
        if ((_enPassentMove & toBit) != 0) {
            // capture the pawn behind it
            long capturedPawn = isWhite ? (toBit >> 8) : (toBit << 8);
            opponentPieces[GameConsts.PAWN] &= ~capturedPawn;

            // handle in graphics
            _listener.onEnPassant(capturedPawn);
        }
    }

    // if a pawn performed a double step, activate en passant
    private void activateEnPassant(boolean isWhite, long fromBit, long toBit) {
        long doubleStep = isWhite ? (fromBit << 16) : (fromBit >> 16);
        
        if ((doubleStep & toBit) != 0) {
            // Set the square the pawn passed through as the en passant target
            _enPassentMove = isWhite ? (fromBit << 8) : (fromBit >> 8);
        } else {
            _enPassentMove = 0L;
        }
    }


    // PROMOTION:

    // check if a pawn has reached the end of the board
    private boolean checkForPawnPromotion(Boolean isWhite, long toBit) {
        if (isWhite){
            return (toBit&GameConsts.RANK_8) != 0;
        }
        return (toBit&GameConsts.RANK_1) != 0;
    }
    
    // promote the piece in the bitboard
    private void promotePieceBitBoard(String promotedPiece, long toBit) {
       // The images are organized in the naming format
        char color = promotedPiece.charAt(0); // 'w' or 'b'
        char type = promotedPiece.charAt(1);  // 'q', 'r', 'b', 'n'
    
        int pieceIndex = switch (type) {
            case 'q' -> GameConsts.QUEEN;
            case 'r' -> GameConsts.ROOK;
            case 'b' -> GameConsts.BISHOP;
            case 'n' -> GameConsts.KNIGHT;
            default  -> -1;
        };
    
        if (pieceIndex != -1){
            if (color == 'w') {
                _whitePieces[pieceIndex] |= toBit;
                _whitePieces[GameConsts.PAWN] &= ~toBit;
            } else if (color == 'b') {
                _blackPieces[pieceIndex] |= toBit;
                _blackPieces[GameConsts.PAWN] &= ~toBit;
            }
        }
    }
    // ================================================================

    // ==================== HANDLE CASTLING ====================

    // if the move is one of the following, castle
    private void handleCastling(String move) {
        switch (move) {
            case "e1g1" -> {
                _whitePieces[GameConsts.ROOK] &= ~(1L << squareToIndex("h1"));
                _whitePieces[GameConsts.ROOK] |=  (1L << squareToIndex("f1"));
                _listener.onCastle("h1f1");
            }
            case "e1c1" -> {
                _whitePieces[GameConsts.ROOK] &= ~(1L << squareToIndex("a1"));
                _whitePieces[GameConsts.ROOK] |=  (1L << squareToIndex("d1"));
                _listener.onCastle("a1d1");
            }
            case "e8g8" -> {
                _blackPieces[GameConsts.ROOK] &= ~(1L << squareToIndex("h8"));
                _blackPieces[GameConsts.ROOK] |=  (1L << squareToIndex("f8"));
                _listener.onCastle("h8f8");
            }
            case "e8c8" -> {
                _blackPieces[GameConsts.ROOK] &= ~(1L << squareToIndex("a8"));
                _blackPieces[GameConsts.ROOK] |=  (1L << squareToIndex("d8"));
                _listener.onCastle("a8d8");
            }
        }
    }
        
    private void handleRookCastleRights(long fromBit, boolean isWhite) {
        if (isWhite) {
            if (fromBit == 1L<<squareToIndex("h1")) _canCastle[0] = false; // h1
            else if (fromBit == 1L<<squareToIndex("a1")) _canCastle[1] = false; // a1
        } else {
            if (fromBit == 1L<<squareToIndex("h8")) _canCastle[2] = false; // h8
            else if (fromBit == 1L<<squareToIndex("a8")) _canCastle[3] = false; // a8
        }
    }

    public long[][] getBoards(boolean isWhite) {
        return new long[][] {
            isWhite?_whitePieces.clone():_blackPieces.clone(),
            isWhite?_blackPieces.clone():_whitePieces.clone(),
            new long[] { _enPassentMove },
            new long[] {
                _canCastle[0] ? 1L : 0L,
                _canCastle[1] ? 1L : 0L,
                _canCastle[2] ? 1L : 0L,
                _canCastle[3] ? 1L : 0L
            }        
        };
    }

    public void setBoards(long[][] boards) {
        _whitePieces = boards[0].clone();
        _blackPieces = boards[1].clone();
        _enPassentMove = boards[2][0];
        _canCastle[0] = boards[3][0] != 0; // White kingside
        _canCastle[1] = boards[3][1] != 0; // White queenside
        _canCastle[2] = boards[3][2] != 0; // Black kingside
        _canCastle[3] = boards[3][3] != 0; // Black queenside
    }

    
    public static String indexToSquare(int index) {
        char file = (char) ('a' + (index % 8));
        int rank = (index / 8) + 1;
        return "" + file + rank;
    }
    
    public static String convertToMoveString(int srcRow, int srcCol, int destRow, int destCol) {
        char srcFile = (char) ('a' + srcCol);
        int srcRank = srcRow + 1;
        char destFile = (char) ('a' + destCol);
        int destRank = destRow + 1;
    
        return "" + srcFile + srcRank + destFile + destRank;
    }   

    
    // returns a specific "square" from a string
    public static int squareToIndex(String square) {
        return (square.charAt(1) - '1') * 8 + (square.charAt(0) - 'a');
    }

    
    
    
}