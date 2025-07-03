package logic;

import consts.GameConsts;
public class Moves {
    
    // ================ Piece Move's ================

    /*
     *  PieceIndex - piece
     *  fromIndex - source square bit
     *  isWhite - for pawn movement (white - up, black - down)
     */
    public static long getLegalMoves(long[][] boards, int pieceIndex, int fromIndex, boolean isWhite) {
        long[] playerBoards = boards[0];
        long[] opponentBoards = boards[1];
        long enPassentBoard = boards[2][0];

        long playerPieces = getAllPieces(playerBoards);
        long opponentPieces = getAllPieces(opponentBoards);

        return switch (pieceIndex) {
            case GameConsts.KING -> getKingMoves(boards, playerPieces, opponentPieces, fromIndex, isWhite, false);
            case GameConsts.QUEEN -> getRookMoves(fromIndex, playerPieces, playerPieces|opponentPieces) | getBishopMoves(fromIndex, playerPieces, playerPieces|opponentPieces);
            case GameConsts.ROOK -> getRookMoves(fromIndex, playerPieces, playerPieces|opponentPieces);
            case GameConsts.BISHOP -> getBishopMoves(fromIndex, playerPieces, playerPieces|opponentPieces);
            case GameConsts.KNIGHT -> getKnightMoves(fromIndex, playerPieces);
            case GameConsts.PAWN -> getPawnMoves(fromIndex, playerPieces, opponentPieces, isWhite, enPassentBoard);
            default -> 0L;
        };
    }

    // ================== KING MOVES ==================
    private static long getKingMoves(long[][] boards, long playerPieces, long opponentPieces, int fromIndex, boolean isWhite, boolean attack) {
        long kingPos = 1L << fromIndex;
        long moves = 0L;
    
        if ((kingPos & GameConsts.RANK_8) == 0) moves |= kingPos << 8;       // Up
        if ((kingPos & GameConsts.RANK_1) == 0) moves |= kingPos >>> 8;      // Down
        if ((kingPos & GameConsts.FILE_H) == 0) moves |= kingPos << 1;       // Right
        if ((kingPos & GameConsts.FILE_A) == 0) moves |= kingPos >>> 1;      // Left
        if ((kingPos & GameConsts.FILE_H) == 0 && (kingPos & GameConsts.RANK_8) == 0) moves |= kingPos << 9;  // Up-Right
        if ((kingPos & GameConsts.FILE_A) == 0 && (kingPos & GameConsts.RANK_8) == 0) moves |= kingPos << 7;  // Up-Left
        if ((kingPos & GameConsts.FILE_H) == 0 && (kingPos & GameConsts.RANK_1) == 0) moves |= kingPos >>> 7; // Down-Right
        if ((kingPos & GameConsts.FILE_A) == 0 && (kingPos & GameConsts.RANK_1) == 0) moves |= kingPos >>> 9; // Down-Left
    
        if (attack) {
            return moves & ~playerPieces;
        }
    
        // If its a real king move (not threat gen)
        if (fromIndex == BitBoard.squareToIndex(isWhite ? "e1" : "e8")) {
            moves |= getCastlingMoves(boards, playerPieces, opponentPieces, isWhite);
        }
    
        // Remove own pieces and threatened squares
        long[][] opponentBoards = new long[][] {
            boards[1].clone(),
            boards[0].clone(),
            new long[] { boards[2][0] },
            boards[3].clone()
        };
    
        moves &= ~playerPieces;
        moves &= ~playerThreats(opponentBoards, !isWhite);
    
        return moves;
    }
    
    // ================== KNIGHT MOVES ==================
    private static long getKnightMoves(int from, long sameColorPieces) {
        long pos = 1L << from;
        long moves = 0L;
    
        moves |= (pos >>> 15) & GameConsts.DDR & ~sameColorPieces;
        moves |= (pos >>> 17) & GameConsts.DDL & ~sameColorPieces;
        moves |= (pos << 17) & GameConsts.UUR & ~sameColorPieces;
        moves |= (pos << 15) & GameConsts.UUL & ~sameColorPieces;
        moves |= (pos >>> 6) & GameConsts.RRU & ~sameColorPieces;
        moves |= (pos << 10) & GameConsts.RRD & ~sameColorPieces;
        moves |= (pos >>> 10) & GameConsts.LLU & ~sameColorPieces;
        moves |= (pos << 6) & GameConsts.LLD & ~sameColorPieces;
    
        return moves;
    }

    // ================== ROOK MOVES ==================
    private static long getRookMoves(int from, long playerPieces, long allPieces) {
        long pos = 1L << from;
        long moves = 0L;
    
        boolean[] stops = {false, false, false, false}; // up, down, right, left
        long tempPos;
    
        for (int i = 1; i < 8 && !(stops[0] && stops[1] && stops[2] && stops[3]); i++) {
            // Up
            if (!stops[0]) {
                tempPos = pos << (8 * i);
                if (tempPos == 0) stops[0] = true;
                else {
                    if ((tempPos & playerPieces) == 0) moves |= tempPos;
                    if ((tempPos & allPieces) != 0) stops[0] = true;
                }
            }
    
            // Down
            if (!stops[1]) {
                tempPos = pos >>> (8 * i);
                if (tempPos == 0) stops[1] = true;
                else {
                    if ((tempPos & playerPieces) == 0) moves |= tempPos;
                    if ((tempPos & allPieces) != 0) stops[1] = true;
                }
            }
    
            // Right
            if (!stops[2]) {
                tempPos = pos >>> i;
                if ((tempPos & GameConsts.FILE_H) != 0) {
                    stops[2] = true;
                } else {
                    if ((tempPos & playerPieces) == 0) moves |= tempPos;
                    if ((tempPos & allPieces) != 0) stops[2] = true;
                }
            }
    
            // Left
            if (!stops[3]) {
                tempPos = pos << i;
                if ((tempPos & GameConsts.FILE_A) != 0) {
                    stops[3] = true;
                } else {
                    if ((tempPos & playerPieces) == 0) moves |= tempPos;
                    if ((tempPos & allPieces) != 0) stops[3] = true;
                }
            }
        }
    
        return moves;
    }

    // BISHOP
    private static long getBishopMoves(int from, long sameColorPieces, long allPieces) {
        long pos = 1L << from;
        long moves = 0L;
    
        boolean stops[] = {false, false, false, false}; // DL, DR, UL, UR
        long tempPos;
    
        for (int i = 1; i < 8 && !(stops[0] && stops[1] && stops[2] && stops[3]); i++) {
            // Down-Left
            if (!stops[0]) {
                tempPos = pos << (9 * i);
                if ((tempPos & GameConsts.FILE_A) != 0) stops[0] = true;
                else {
                    if ((tempPos & sameColorPieces) != 0) stops[0] = true;
                    else {
                        moves |= tempPos;
                        if ((tempPos & allPieces) != 0) stops[0] = true;
                    }
                }
            }
    
            // Down-Right
            if (!stops[1]) {
                tempPos = pos << (7 * i);
                if ((tempPos & GameConsts.FILE_H) != 0) stops[1] = true;
                else {
                    if ((tempPos & sameColorPieces) != 0) stops[1] = true;
                    else {
                        moves |= tempPos;
                        if ((tempPos & allPieces) != 0) stops[1] = true;
                    }
                }
            }
    
            // Up-Left
            if (!stops[2]) {
                tempPos = pos >>> (7 * i);
                if ((tempPos & GameConsts.FILE_A) != 0) stops[2] = true;
                else {
                    if ((tempPos & sameColorPieces) != 0) stops[2] = true;
                    else {
                        moves |= tempPos;
                        if ((tempPos & allPieces) != 0) stops[2] = true;
                    }
                }
            }
    
            // Up-Right
            if (!stops[3]) {
                tempPos = pos >>> (9 * i);
                if ((tempPos & GameConsts.FILE_H) != 0) stops[3] = true;
                else {
                    if ((tempPos & sameColorPieces) != 0) stops[3] = true;
                    else {
                        moves |= tempPos;
                        if ((tempPos & allPieces) != 0) stops[3] = true;
                    }
                }
            }
        }
    
        return moves;
    }
    
    // ================== PAWN MOVES ==================
    private static long getPawnMoves(int from, long playerPieces, long opponentPieces, boolean isWhite, long enPassentBoard) {
        long moves = 0L;
        long pawnPos = 1L << from;
    
        // Forward move by 1
        long forwardOne = isWhite ? (pawnPos << 8) : (pawnPos >> 8);
        if ((forwardOne & (playerPieces | opponentPieces)) == 0) {
            moves |= forwardOne;
    
            // Forward move by 2 (from starting rank)
            boolean atStartRank = isWhite ?
                (pawnPos & GameConsts.RANK_2) != 0 :
                (pawnPos & GameConsts.RANK_7) != 0;
    
            long forwardTwo = isWhite ? (pawnPos << 16) : (pawnPos >> 16);
            if (atStartRank && (forwardTwo & (playerPieces | opponentPieces)) == 0) {
                moves |= forwardTwo;
            }
        }
    
        // Captures
        if (isWhite) {
            if ((pawnPos & GameConsts.FILE_A) == 0) {
                long leftCapture = pawnPos << 7;
                if ((leftCapture & opponentPieces) != 0 ||
                    ((leftCapture & enPassentBoard) != 0 && (leftCapture & 0x0000FF0000000000L) != 0)) {
                    moves |= leftCapture;
                }
            }
            if ((pawnPos & GameConsts.FILE_H) == 0) {
                long rightCapture = pawnPos << 9;
                if ((rightCapture & opponentPieces) != 0 ||
                    ((rightCapture & enPassentBoard) != 0 && (rightCapture & 0x0000FF0000000000L) != 0)) {
                    moves |= rightCapture;
                }
            }
        } else {
            if ((pawnPos & GameConsts.FILE_A) == 0) {
                long leftCapture = pawnPos >>> 9;
                if ((leftCapture & opponentPieces) != 0 ||
                    ((leftCapture & enPassentBoard) != 0 && (leftCapture & 0x00000000FF000000L) != 0)) {
                    moves |= leftCapture;
                }
            }
            if ((pawnPos & GameConsts.FILE_H) == 0) {
                long rightCapture = pawnPos >>> 7;
                if ((rightCapture & opponentPieces) != 0 ||
                    ((rightCapture & enPassentBoard) != 0 && (rightCapture & 0x00000000FF000000L) != 0)) {
                    moves |= rightCapture;
                }
            }
        }
        return moves;
    }
     
    
    private static long getPawnAttacks(int from, boolean isWhite) {
        long pos = 1L << from;
        long attacks = 0L;
    
        if (isWhite) {
            if ((pos & GameConsts.FILE_A) == 0) attacks |= pos << 7; // Left attack
            if ((pos & GameConsts.FILE_H) == 0) attacks |= pos << 9; // Right attack
        } else {
            if ((pos & GameConsts.FILE_A) == 0) attacks |= pos >>> 9; // Left attack
            if ((pos & GameConsts.FILE_H) == 0) attacks |= pos >>> 7; // Right attack
        }
    
        return attacks;
    }
    
    // ================== CASTLING MOVES ==================
    public static long getCastlingMoves(long[][] boards, long playerPieces, long opponentPieces, boolean isWhite) {
        long moves = 0L;

        long allPieces = playerPieces | opponentPieces;

        long[][] opponentBoards = new long[][] {
            boards[1].clone(),
            boards[0].clone(),
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        long opponentThreats = playerThreats(opponentBoards, !isWhite);

        // Castling bitmasks
        long kingSideClear = isWhite ? 0x0000000000000060L : 0x6000000000000000L;
        long queenSideClear = isWhite ? 0x000000000000000EL : 0x0E00000000000000L;

        long kingSideSafe = isWhite ? 0x0000000000000070L : 0x7000000000000000L;
        long queenSideSafe = isWhite ? 0x000000000000001CL : 0x1C00000000000000L;

        long kingSideMove = isWhite ? 0x0000000000000040L : 0x4000000000000000L;
        long queenSideMove = isWhite ? 0x0000000000000004L : 0x0400000000000000L;

        // Castling rights from boards[3]
        boolean canCastleKingSide = boards[3][isWhite ? 0 : 2] != 0L;
        boolean canCastleQueenSide = boards[3][isWhite ? 1 : 3] != 0L;

        if (canCastleKingSide &&
            (allPieces & kingSideClear) == 0 &&
            (opponentThreats & kingSideSafe) == 0) {
            moves |= kingSideMove;
        }

        if (canCastleQueenSide &&
            (allPieces & queenSideClear) == 0 &&
            (opponentThreats & queenSideSafe) == 0) {
            moves |= queenSideMove;
        }

        return moves;
    }
        
    // gets boards and check if the player (boards[0]) is in check
    public static boolean isInCheck(long[][] boards, boolean isWhite) {
        long kingBoard = boards[0][GameConsts.KING];

        long[][] opponentBoards = new long[][] {
            boards[1].clone(),  // *opposite king*
            boards[0].clone(),
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        long threatsBoard = playerThreats(opponentBoards, !isWhite);
        return (kingBoard & threatsBoard) != 0;
    }
    
    public static boolean hasAnyLegalMove(long[][] boards, boolean isWhite) {    
        long[] playerPieces = boards[0];
    
        for (int type = 0; type < 6; type++) {
            long pieceboard = playerPieces[type];
    
            for (int fromIndex = 0; fromIndex < GameConsts.NUM_OF_SQUARES; fromIndex++) {
                long fromBit = 1L << fromIndex;
                if ((pieceboard & fromBit) != 0) {
    
                    long legalMoves = getLegalMoves(boards, type, fromIndex, isWhite);
    
                    for (int toIndex = 0; toIndex < 64; toIndex++) {
                        long toBit = 1L << toIndex;
                        if ((legalMoves & toBit) != 0 &&
                            isMoveLegal(boards, type, fromBit, toBit, isWhite)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static long playerThreats(long[][] boards, boolean isWhite) {
        long threats = 0L;
        long[] playerBoards = boards[0];
    
        for (int type = 0; type < 6; type++) {
            long pieceBoard = playerBoards[type];
    
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                if (((pieceBoard >> from) & 1) == 1) {
                    switch (type) {
                        case GameConsts.PAWN -> threats |= getPawnAttacks(from, isWhite);
                        case GameConsts.KING -> threats |= getKingMoves(boards, threats, threats, from, isWhite, true); // disable castling during threat calc
                        default -> threats |= getLegalMoves(boards, type, from, isWhite);
                    }
                }
            }
        }
        return threats;
    }

    // Make a move on a demo boards and returns true if it doesn’t leave the player's king in check (legal move).
    public static boolean isMoveLegal(long[][] boards, int pieceIndex, long fromBit, long toBit, boolean isWhite) {
        long[] tempPlayer = boards[0].clone();
        long[] tempOpponent = boards[1].clone();
    
        tempPlayer[pieceIndex] &= ~fromBit;
        tempPlayer[pieceIndex] |= toBit;
    
        for (int i = 0; i < 6; i++) {
            tempOpponent[i] &= ~toBit;
        }
    
        long[][] newBoards = new long[][] {
            tempPlayer,
            tempOpponent,
            new long[] { boards[2][0] },
            boards[3].clone()
        };
    
        boolean legal = !isInCheck(newBoards, isWhite);    
        return legal;
    }

    // returns a board with all of the pieces
    public static long getAllPieces(long[] pieces) {
        long all = 0L;
        for (long p : pieces) all |= p;
        return all;
    }
    
    // For Debugging Purposes - Not used in the program - can be used to present the user his moves
    public static void printLegalMoves(long legalMoves) {
        //System.out.println("Legal moves for " + 1 + ":");
    
        for (int rank = 7; rank >= 0; rank--) { // Start from rank 7 (top row) to rank 0 (bottom row)
            for (int file = 0; file < 8; file++) { // left to right
                int squareIndex = rank * 8 + file;
    
                if ((legalMoves & (1L << squareIndex)) != 0) {
                    System.out.print(" " + 1 + "  "); // Show piece's move
                } else {
                    System.out.print(" .  "); // Show empty squares
                }
            }
            System.out.println(); // Newline after each row
        }
        System.out.println();
    }

    
    public static void printAllBoards(long[][] boards) {
        System.out.println("========= WHITE PIECES =========");
        for (int i = 0; i < 6; i++) {
            System.out.println("White " + GameConsts.SYMBOLS[i]);
            Moves.printLegalMoves(boards[0][i]);
        }
    
        System.out.println("========= BLACK PIECES =========");
        for (int i = 0; i < 6; i++) {
            System.out.println("Black " + GameConsts.SYMBOLS[i]);
            Moves.printLegalMoves(boards[1][i]);
        }
    }

}
