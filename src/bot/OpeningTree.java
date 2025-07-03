package bot;
import consts.GameConsts;
import java.util.Random;
import logic.BitBoard;
import logic.Moves;

public class OpeningTree {
    private Node _root;
    private boolean _isBotWhite;

    public OpeningTree(boolean isBotWhite) {
        _isBotWhite = isBotWhite;
        buildTree();
    }

    private void buildTree() {
        // ====== Create Answer Nodes ======
        AnswerNode ans4  = new AnswerNode(4,this::ans4);
        AnswerNode ans5  = new AnswerNode(5,this::ans5);
        AnswerNode ans8  = new AnswerNode(8,this::ans8);
        AnswerNode ans12 = new AnswerNode(12,this::ans12);
        AnswerNode ans14 = new AnswerNode(14,this::ans14);
        AnswerNode ans16 = new AnswerNode(16,this::ans16);
        AnswerNode ans17 = new AnswerNode(17, func -> "DONE");
        AnswerNode ans18 = new AnswerNode(18, this::ans18);
        AnswerNode ans19 = new AnswerNode(19, this::ans19);

        // ====== Create Question Nodes ======
        QuestionNode q1  = new QuestionNode(1, this::q1);
        QuestionNode q2  = new QuestionNode(2, this::q2);
        QuestionNode q3  = new QuestionNode(3, this::q3);
        QuestionNode q6  = new QuestionNode(6, this::q6);
        QuestionNode q7  = new QuestionNode(7, this::q7);
        QuestionNode q9  = new QuestionNode(9, this::q9);
        QuestionNode q10 = new QuestionNode(10, this::q10);
        QuestionNode q11 = new QuestionNode(11, this::q11);
        QuestionNode q13 = new QuestionNode(13, this::q13);
        QuestionNode q15 = new QuestionNode(15, this::q15);
        QuestionNode q20 = new QuestionNode(20, this::q20);
        
        // ====== Connect Nodes (Set Yes/No) ======
        q1.setYes(q2);
        q1.setNo(q3);

        q2.setYes(q20);
        q2.setNo(ans5);

        q20.setYes(ans4);
        q20.setNo(ans5);

        q3.setYes(q6);
        q3.setNo(q7);

        q6.setYes(ans8);
        q6.setNo(q9);

        q9.setYes(ans12);
        q9.setNo(q13);

        q13.setYes(ans17);
        q13.setNo(ans18);

        q7.setYes(q10);
        q7.setNo(q11);

        q10.setYes(ans14);
        q10.setNo(q15);

        q15.setYes(ans19);
        q15.setNo(ans5);

        q11.setYes(ans16);
        q11.setNo(q10);

        // ====== Set Root ======
        _root = q1;
    }

    public String run(long[][] boards) {
        Node current = _root;
        while (current instanceof QuestionNode) {
            QuestionNode q = (QuestionNode) current;
            boolean answer = q.test(boards.clone());
            current = q.next(answer);
            System.out.println("Question " + q.getAct() + " -> Answer: " + (answer ? "YES" : "NO")); // for dubugging purpuses
        }
        String answer = ((AnswerNode) current).getMove(boards.clone());
        System.out.println("Answer Node " + current.getAct() + " -> Move: " + answer); // for debugging purpuses
        return (answer == null)?ans5(boards.clone()):answer;
    }

    // =========================
    // ====== Questions ========
    // =========================

    private boolean q1(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];
        String[] squares = _isBotWhite ? new String[] { "e2", "d2" } : new String[] { "e7", "d7" };

        for (String square : squares) {
            int from = BitBoard.squareToIndex(square);

            if (((pawns >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, from, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.PAWN, 1L<<from, 1L<<to, _isBotWhite)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean q2(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];

        if (_isBotWhite) {
            boolean onE4 = ((pawns >> BitBoard.squareToIndex("e4")) & 1) == 1;
            boolean onD4 = ((pawns >> BitBoard.squareToIndex("d4")) & 1) == 1;
            return onE4 || onD4;
        } else {
            boolean onE5 = ((pawns >> BitBoard.squareToIndex("e5")) & 1) == 1;
            boolean onD5 = ((pawns >> BitBoard.squareToIndex("d5")) & 1) == 1;
            return onE5 || onD5;
        }
    }

    private boolean q20(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];
        String[] centerSquares = _isBotWhite ? new String[]{"e4", "d4"} : new String[]{"e5", "d5"};
    
        for (String from : centerSquares) {
            int fromIndex = BitBoard.squareToIndex(from);
            long fromBit = 1L << fromIndex;
    
            if ((pawns & fromBit) != 0) {
                long legalMoves = Moves.getLegalMoves(boards,GameConsts.PAWN, fromIndex, _isBotWhite);

                    if (legalMoves != 0) {
                        return true;
                }
            }
        }
        return false;
    }
    
    private boolean q3(long[][] boards) {
        long knights = boards[0][GameConsts.KNIGHT];
        long bishops = boards[0][GameConsts.BISHOP];

        if (_isBotWhite) {
            boolean knight1Moved = ((knights >> BitBoard.squareToIndex("b1")) & 1) == 0;
            boolean knight2Moved = ((knights >> BitBoard.squareToIndex("g1")) & 1) == 0;

            boolean bishop1Moved = ((bishops >> BitBoard.squareToIndex("c1")) & 1) == 0;
            boolean bishop2Moved = ((bishops >> BitBoard.squareToIndex("f1")) & 1) == 0;

            return knight1Moved && knight2Moved && bishop1Moved && bishop2Moved;
        } else {
            boolean knight1Moved = ((knights >> BitBoard.squareToIndex("b8")) & 1) == 0;
            boolean knight2Moved = ((knights >> BitBoard.squareToIndex("g8")) & 1) == 0;

            boolean bishop1Moved = ((bishops >> BitBoard.squareToIndex("c8")) & 1) == 0;
            boolean bishop2Moved = ((bishops >> BitBoard.squareToIndex("f8")) & 1) == 0;

            return knight1Moved && knight2Moved && bishop1Moved && bishop2Moved;
        }
    }

    private boolean q6(long[][] boards) {
        long castlingMoves = Moves.getCastlingMoves(boards, Moves.getAllPieces(boards[0]), Moves.getAllPieces(boards[1]), _isBotWhite);
    
        int kingSideCastle = BitBoard.squareToIndex(_isBotWhite ? "g1" : "g8");
        int queenSideCastle = BitBoard.squareToIndex(_isBotWhite ? "c1" : "c8");
    
        boolean canCastleKingSide = ((castlingMoves >> kingSideCastle) & 1) == 1;
        boolean canCastleQueenSide = ((castlingMoves >> queenSideCastle) & 1) == 1;
    
        return canCastleKingSide || canCastleQueenSide;
    }

    private boolean q7(long[][] boards) {
        long knights = boards[0][GameConsts.KNIGHT];
        return ((knights >> BitBoard.squareToIndex(_isBotWhite ? "b1" : "b8")) & 1) == 0 &&
           ((knights >> BitBoard.squareToIndex(_isBotWhite ? "g1" : "g8")) & 1) == 0;
    }

    // q9 - Checks if there are any open files (no pieces on it). O(n)
    private boolean q9(long[][] boards) {
        long allPieces = Moves.getAllPieces(boards[0]) | Moves.getAllPieces(boards[1]);
        long rooks = boards[0][GameConsts.ROOK];

        for (int file = 0; file < 8; file++) {
            long fileMask = 0x0101010101010101L << file;

            // Is the file empty?
            if ((allPieces & fileMask) == 0) {
                // if there is already a rook on the empty file
                if ((rooks & fileMask) != 0) {
                    return true;
                }
            }
        }

        return false;
    }


    private boolean q10(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];

        boolean queenSideBlocked = 
            ((pawns >> BitBoard.squareToIndex(_isBotWhite ? "b2" : "b7")) & 1) == 1 ||
            ((pawns >> BitBoard.squareToIndex(_isBotWhite ? "d2" : "d7")) & 1) == 1;

        boolean kingSideBlocked = 
            ((pawns >> BitBoard.squareToIndex(_isBotWhite ? "g2" : "g7")) & 1) == 1 ||
            ((pawns >> BitBoard.squareToIndex(_isBotWhite ? "e2" : "e7")) & 1) == 1;

        return queenSideBlocked && kingSideBlocked;
    }
    
    private boolean q11(long[][] boards) {
        long knights = boards[0][GameConsts.KNIGHT];
    
        for (int from = 0; from < GameConsts.NUM_OF_SQUARES && knights != 0; from++, knights >>>= 1) {
            if ((knights & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards,GameConsts.KNIGHT, from, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1) {
                        if (Bot.isSafeMove(boards, GameConsts.BISHOP, 1L << from, 1L << to, _isBotWhite)) {
                            return true;
                        }   
                    }
                }
            }
        }
    
        return false;
    }
    
    private boolean q13(long[][] boards) {
        return ((boards[0][GameConsts.QUEEN] >> BitBoard.squareToIndex(_isBotWhite?"d1":"d8")) & 1) == 0;
    }
        
    private boolean q15(long[][] boards) {
        long bishops = boards[0][GameConsts.BISHOP];

        for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
            if (((bishops >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.BISHOP, from, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1){
                        if (Bot.isSafeMove(boards, GameConsts.BISHOP, 1L << from, 1L << to, _isBotWhite)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

        
    

    // =========================
    // ====== Answers ========== 
    // =========================

    private String ans4(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];
        String[] centerSquares = _isBotWhite ? new String[]{"e4", "d4"} : new String[]{"e5", "d5"};
    
        for (String from : centerSquares) {
            int fromIndex = BitBoard.squareToIndex(from);
            if (((pawns >> fromIndex) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, fromIndex, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.PAWN, 1L<<fromIndex, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(fromIndex / 8, fromIndex % 8, to / 8, to % 8);
                    }
                }
            }
        }
        return null;
    }    

    // Opening fallback
    private String ans5(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];

        String[] priorityMoves = _isBotWhite ? new String[] {
            "e2e4", "d2d4",
            "d2d3", "e2e3",
            "c2c4", "f2f4",
            "c2c3", "f2f3", "g2g3", "b2b3",
            "a2a3", "h2h3", "h2h4"
        } : new String[] {
            "e7e5", "d7d5",
            "d7d6", "e7e6",
            "c7c5", "f7f5",
            "c7c6", "f7f6", "g7g6", "b7b6",
            "a7a6", "h7h6", "h7h5"
        };
        
        Random rand = new Random();
        if (rand.nextBoolean()) {
            // Swap the first two moves
            String temp = priorityMoves[0];
            priorityMoves[0] = priorityMoves[1];
            priorityMoves[1] = temp;
        }

        for (String move : priorityMoves) {
            int from = BitBoard.squareToIndex(move.substring(0, 2));
            int to = BitBoard.squareToIndex(move.substring(2, 4));
            // if there is a pawn in the src square
            if (((pawns>>from)&1)==1)
            {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, from, _isBotWhite);
                if (((legalMoves >> to) & 1) == 1 &&
                    Bot.isSafeMove(boards, GameConsts.PAWN, 1L << from, 1L << to, _isBotWhite)) {
                    return move;
                }
            }
        }
    
        for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
            if (((pawns >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, from, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.PAWN, 1L << from, 1L << to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
            }
        }
    
        return null;
    }
    
    
    private String ans8(long[][] boards) {
        //int kingFrom = BitBoard.squareToIndex(_isBotWhite ? "e1" : "e8");
    
        int kingSideTo = BitBoard.squareToIndex(_isBotWhite ? "g1" : "g8");
        int queenSideTo = BitBoard.squareToIndex(_isBotWhite ? "c1" : "c8");
    
        long castlingMoves = Moves.getCastlingMoves(boards, Moves.getAllPieces(boards[0]), Moves.getAllPieces(boards[1]), _isBotWhite);
    
        if (((castlingMoves >> kingSideTo) & 1L) == 1) {
            return _isBotWhite ? "e1g1" : "e8g8";
        }
    
        if (((castlingMoves >> queenSideTo) & 1L) == 1) {
            return _isBotWhite ? "e1c1" : "e8c8";
        }
    
        return null;
    }
    
    private String ans12(long[][] boards) {
        long allPieces = Moves.getAllPieces(boards[0]) | Moves.getAllPieces(boards[1]);
        long rooks = boards[0][GameConsts.ROOK];
    
        for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
            if (((rooks >> from) & 1) == 1) {
                int fromRow = from / 8;
                for (int file = 0; file < GameConsts.NUM_OF_ROWS; file++) {
                    int to = fromRow * 8 + file;
                    if (to != from && ((allPieces & (1L << to)) == 0)) {
                        long legalMoves = Moves.getLegalMoves(boards, GameConsts.ROOK, from, _isBotWhite);
                        if (((legalMoves >> to) & 1) == 1 &&
                            Bot.isSafeMove(boards, GameConsts.ROOK, 1L<<from, 1L<<to, _isBotWhite)) {
                            return BitBoard.convertToMoveString(fromRow, from % 8, fromRow, file);
                        }
                    }
                }
            }
        }
    
        return null;
    }
   
    private String ans14(long[][] boards) {
        long pawns = boards[0][GameConsts.PAWN];
        String[] options = _isBotWhite ? 
            new String[]{"b2", "g2", "d2", "e2"} : 
            new String[]{"b7", "g7", "d7", "e7"};
    
        for (String start : options) {
            int from = BitBoard.squareToIndex(start);
            if (((pawns >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, from, _isBotWhite);
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.PAWN, 1L<<from, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
            }
        }
    
        return null;
    }

    private String ans16(long[][] boards) {
        long knights = boards[0][GameConsts.KNIGHT];
    
        String[] startSquares = _isBotWhite ? new String[]{"b1", "g1"} : new String[]{"b8", "g8"};

        Random rand = new Random();
        if (rand.nextBoolean()) {
            String temp = startSquares[0];
            startSquares[0] = startSquares[1];
            startSquares[1] = temp;
        }

        String[] priorityTargets = _isBotWhite ?
            new String[]{"c3", "f3", "d4", "e4"} :
            new String[]{"c6", "f6", "d5", "e5"};
    
        for (String start : startSquares) {
            int from = BitBoard.squareToIndex(start);
            if (((knights >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards,GameConsts.KNIGHT, from, _isBotWhite);
    
                for (String target : priorityTargets) {
                    int to = BitBoard.squareToIndex(target);
                    if (((legalMoves >> to) & 1) == 1 && Bot.isSafeMove(boards, GameConsts.KNIGHT, 1L<<from, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
    
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.KNIGHT, 1L<<from, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
            }
        }
        return null;
    }

    private String ans18(long[][] boards) {
        long queens = boards[0][GameConsts.QUEEN];
        int from = BitBoard.squareToIndex(_isBotWhite ? "d1" : "d8");
    
        if (((queens >> from) & 1) == 1) {
            long legalMoves = Moves.getLegalMoves(boards,GameConsts.QUEEN, from, _isBotWhite);
            
            String[] targets = _isBotWhite ?
                new String[]{"d2", "e2", "c3", "d3", "e3"} :
                new String[]{"d7", "e7", "c6", "d6", "e6"};
    
            for (String target : targets) {
                int to = BitBoard.squareToIndex(target);
                if (((legalMoves >> to) & 1) == 1 &&
                    Bot.isSafeMove(boards, GameConsts.QUEEN, 1L<<from, 1L<<to, _isBotWhite)) {
                    return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                }
            }
    
            for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                if (((legalMoves >> to) & 1) == 1 &&
                    Bot.isSafeMove(boards, GameConsts.QUEEN, 1L<<from, 1L<<to, _isBotWhite)) {
                    return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                }
            }
        }
    
        return null;
    }
    
    
        
    private String ans19(long[][] boards) {
        long bishops = boards[0][GameConsts.BISHOP];
    
        String[] startingSquares = _isBotWhite ? new String[]{"c1", "f1"} : new String[]{"c8", "f8"};
        String[] priorityTargets = new String[]{"d4", "e4", "d5", "e5", "c4", "f4", "c5", "f5"};
    
        for (String start : startingSquares) {
            int from = BitBoard.squareToIndex(start);
            if (((bishops >> from) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards,GameConsts.BISHOP, from, _isBotWhite);
    
                for (String target : priorityTargets) {
                    int to = BitBoard.squareToIndex(target);
                    if (((legalMoves >> to) & 1) == 1 && Bot.isSafeMove(boards, GameConsts.BISHOP, 1L<<from, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
    
                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    if (((legalMoves >> to) & 1) == 1 &&
                        Bot.isSafeMove(boards, GameConsts.BISHOP, 1L<<from, 1L<<to, _isBotWhite)) {
                        return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                    }
                }
            }
        }
    
        return null;
    }
}