package bot;
import consts.GameConsts;
import logic.BitBoard;
import logic.Moves;

public class MiddleTree {
    private Node _root;
    private boolean _isBotWhite;

    public MiddleTree(boolean isBotWhite) {
        _isBotWhite = isBotWhite;
        buildTree();
    }

    private void buildTree() {

        // ====== Create Question Nodes ======
        QuestionNode q21 = new QuestionNode(21, this::q21);
        QuestionNode q22 = new QuestionNode(22, this::q22);
        QuestionNode q23 = new QuestionNode(23, this::q23);
        QuestionNode q25 = new QuestionNode(25, this::q25);
        QuestionNode q6  = new QuestionNode(6,  this::q6);
        QuestionNode q9  = new QuestionNode(9,  this::q9);
        QuestionNode q31 = new QuestionNode(31, this::q31);
        QuestionNode q34 = new QuestionNode(34, this::q34);

        // ====== Create Answer Nodes ======
        AnswerNode ans28 = new AnswerNode(28, this::ans28);
        AnswerNode ans29 = new AnswerNode(29, this::ans29);
        AnswerNode ans8  = new AnswerNode(8,  this::ans8);
        AnswerNode ans12 = new AnswerNode(12, this::ans12);
        AnswerNode ans36 = new AnswerNode(36, this::ans36);
        AnswerNode ans37 = new AnswerNode(37, this::ans37);
        AnswerNode ans38 = new AnswerNode(38, this::ans38);

        // ====== Connect Nodes (Set Yes/No) ======
        q21.setYes(q22);
        q21.setNo(q23);

        q22.setYes(ans28);
        q22.setNo(q25);

        q25.setYes(ans29);
        q25.setNo(q23);

        q23.setYes(q6);
        q23.setNo(q9);

        q6.setYes(ans8);
        q6.setNo(q31);

        q31.setYes(ans36);
        q31.setNo(q9);

        q9.setYes(ans12);
        q9.setNo(q34);

        q34.setYes(ans37);
        q34.setNo(ans38);

        // ====== Set Root ======
        _root = q21;
    }

    public String run(long[][] boards) {
        Node current = _root;
        while (current instanceof QuestionNode) {
            QuestionNode q = (QuestionNode) current;
            boolean answer = q.test(boards.clone());
            System.out.println("Question " + q.getAct() + " -> Answer: " + (answer ? "YES" : "NO"));
            current = q.next(answer);
        }
        String answer = ((AnswerNode) current).getMove(boards.clone());
        System.out.println("Answer Node " + current.getAct() + " -> Move: " + answer);
        return (answer == null)?ans38(boards.clone()):answer;
    }
    
    // q6 - Checks if castling is possible. O(1).
    private boolean q6(long[][] boards) {
        long castlingMoves = Moves.getCastlingMoves(boards, Moves.getAllPieces(boards[0]), Moves.getAllPieces(boards[1]), _isBotWhite);
    
        int kingSideCastle = BitBoard.squareToIndex(_isBotWhite ? "g1" : "g8");
        int queenSideCastle = BitBoard.squareToIndex(_isBotWhite ? "c1" : "c8");
    
        boolean canCastleKingSide = ((castlingMoves >> kingSideCastle) & 1) == 1;
        boolean canCastleQueenSide = ((castlingMoves >> queenSideCastle) & 1) == 1;
    
        return canCastleKingSide || canCastleQueenSide;
    }

    // q9 - Checks if there are any open files (no pieces on it). O(n)
    private boolean q9(long[][] boards) {
        long allPieces = Moves.getAllPieces(boards[0]) | Moves.getAllPieces(boards[1]);
        long rooks = boards[0][GameConsts.ROOK];

        for (int file = 0; file < GameConsts.NUM_OF_ROWS; file++) {
            long fileMask = 0x0101010101010101L << file;

            // Is the file empty?
            if ((allPieces & fileMask) == 0) {
                // Does the bot have a rook on that file?
                if ((rooks & fileMask) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // q21 - Checks if any pieces of the bot is under threat. O(n^2)
    private boolean q21(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        long playerThreat = Moves.playerThreats(playerBoards, !_isBotWhite);
    
        long noPawnsBot = 0L;
        for (int i = 0; i < 5; i++) {
            noPawnsBot |= bot[i];
        }
    
        return (playerThreat & noPawnsBot) != 0;
    }
    
    private boolean q22(long[][] boards) {
        int trade = isGoodTrade(boards.clone());
        int materialBalance = getMaterialBalance(boards.clone());

        return trade < 0 || (trade == 0 && materialBalance >= 0);
    }
    
    // q23 - Checks if any major piece is still on its initial square. O(1)
    private boolean q23(long[][] boards) {
        long[] bot = boards[0];
    
        long initialKnights = _isBotWhite ? 0x0000000000000042L : 0x4200000000000000L;
        long initialBishops = _isBotWhite ? 0x0000000000000024L : 0x2400000000000000L;
        long initialRooks   = _isBotWhite ? 0x0000000000000081L : 0x8100000000000000L;
        long initialQueen   = _isBotWhite ? 0x0000000000000008L : 0x0800000000000000L;
    
        return ((bot[GameConsts.KNIGHT] & initialKnights) != 0) ||
                ((bot[GameConsts.BISHOP] & initialBishops) != 0) ||
                ((bot[GameConsts.ROOK]   & initialRooks) != 0) ||
                ((bot[GameConsts.QUEEN]  & initialQueen) != 0);
    }
    
    private boolean q25(long[][] boards) {
        return moveAttackedPiece(boards) != null;
    }
    
    private boolean q31(long[][] boards) {
        return developPieces(boards) != null;
    }
    
    private boolean q34(long[][] boards) {
        return findUnprotectedEnemySquare(boards) != null;
    }
    
    // ======================== ANSWERS ========================
    private String ans8(long[][] boards) { 
        long castlingMoves = Moves.getCastlingMoves(boards, Moves.getAllPieces(boards[0]), Moves.getAllPieces(boards[1]), _isBotWhite);

        if (((castlingMoves >> BitBoard.squareToIndex(_isBotWhite?"g1":"g8")) & 1L) == 1)
        {
            return _isBotWhite?"e1g1":"e8g8";
        }
        else if (((castlingMoves >> BitBoard.squareToIndex(_isBotWhite?"c1":"c8")) & 1L) == 1)
        {
            return _isBotWhite?"e1c1":"e8c8";
        }
        return null;
    }
    
    private String ans12(long[][] boards) {
        long allPieces = Moves.getAllPieces(boards[0]) | Moves.getAllPieces(boards[1]);
        long rooks = boards[0][GameConsts.ROOK];
    
        int fromIndex = Long.numberOfTrailingZeros(rooks);
        int fromRow = fromIndex / 8;
    
        for (int file = 0; file < GameConsts.NUM_OF_ROWS; file++) {
            long fileMask = 0x0101010101010101L << file;
            if ((allPieces & fileMask) == 0) {
                return BitBoard.convertToMoveString(fromRow, fromIndex % 8, fromRow, file);
            }
        }
    
        return null;
    }

    private String ans28(long[][] boards) {
        int[] threat = getMaxValThreatsAgainstBot(boards); // {fromIndex, toIndex}
        if (threat != null){     
            // bot's piece being attacked and then attacker's square
            String move = BitBoard.indexToSquare(threat[0]) + BitBoard.indexToSquare(threat[1]);
            return move;
        }
        return null;
    }

    private String ans29(long[][] boards) {
        return moveAttackedPiece(boards);
    }
    
    private String ans36(long[][] boards) {
        return developPieces(boards);
    }

    private String ans37(long[][] boards) {
        return findUnprotectedEnemySquare(boards);
    }

    private String ans38(long[][] boards) {
        return findBestMove(boards);
    }

    private int getMaterialBalance(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];

        int botScore = 0;
        int playerScore = 0;

        for (int i = 1; i < 6; i++) {
            botScore += Long.bitCount(bot[i]) * GameConsts.PIECE_VALS[i - 1];
            playerScore += Long.bitCount(player[i]) * GameConsts.PIECE_VALS[i - 1];
        }

        return botScore - playerScore;
    }

    private int isGoodTrade(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };
    
        int bestDiff = Integer.MIN_VALUE;
    
        for (int playerType = 1; playerType < 6; playerType++) {
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                if (((player[playerType] >> from) & 1) == 1) {
                    long legalMoves = Moves.getLegalMoves(playerBoards, playerType, from, !_isBotWhite);
                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((legalMoves >> to) & 1) == 1) {
                            for (int botType = 1; botType < 6; botType++) {
                                if (((bot[botType] >> to) & 1) == 1) {
                                    int diff = GameConsts.PIECE_VALS[botType] - GameConsts.PIECE_VALS[playerType];
                                    bestDiff = Math.max(bestDiff, diff);
                                }
                            }
                        }
                    }
                }
            }
        }
    
        return bestDiff;
    }
    

    private int[] getMaxValThreatsAgainstBot(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];

        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        int bestDiff = Integer.MIN_VALUE;
        int[] bestMove = null;


        // for each piece on the player boards check if one of his legal moves is to attack one of the bot's pieces
        for (int playerType = 1; playerType < 6; playerType++) {
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                if (((player[playerType] >> from) & 1) == 1) {
                    long oppoMoves = Moves.getLegalMoves(playerBoards, playerType, from, !_isBotWhite);

                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((oppoMoves >> to) & 1) == 1) {
                            for (int botType = 1; botType < 6; botType++) {
                                if (((bot[botType] >> to) & 1) == 1) {
                                    for (int myFrom = 0; myFrom < GameConsts.NUM_OF_SQUARES; myFrom++) {
                                        if (((bot[botType] >> myFrom) & 1) == 1) {
                                            long legal = Moves.getLegalMoves(boards, botType, myFrom, _isBotWhite);

                                            // attack the player src square
                                            if (((legal >> from) & 1) == 1 &&
                                                Moves.isMoveLegal(boards, botType, 1L << myFrom, 1L << from, _isBotWhite)) {

                                                int diff = GameConsts.PIECE_VALS[botType] - GameConsts.PIECE_VALS[playerType];

                                                if (diff > bestDiff) {
                                                    bestDiff = diff;
                                                    bestMove = new int[] { myFrom, from }; // from is the opponent's pos
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return bestMove; // we cant capture the attacker
    }

    
    private String findBestMove(long[][] boards) {
        long[] bot = boards[0];

        String bestMove = null;
        int max = -1;
    
        for (int type = 0; type < 6; type++) {
            long pieces = bot[type];
    
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                long fromBit = 1L << from;
    
                if ((pieces & fromBit) != 0) {
                    long legalMoves = Moves.getLegalMoves(boards,type, from, _isBotWhite);
    
                    int count = Long.bitCount(legalMoves);
    
                    if (count > max) {
                        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                            if (((legalMoves >> to) & 1) == 1 &&
                            Bot.isSafeMove(boards, type, fromBit, 1L<<to, _isBotWhite)) {
                                max = count;
                                bestMove = BitBoard.indexToSquare(from) + BitBoard.indexToSquare(to);
                            }
                        }
                    }
                }
            }
        }
    
        return bestMove;
    }
    
    public String developPieces(long[][] boards) {
        long[] bot = boards[0];
    
        int row = _isBotWhite ? 0 : 7;
        int start = row * 8;
        int end = start + 8;
    
        long[] initialPositions = _isBotWhite
        ? new long[] {
            0x0000000000000010L, // King
            0x0000000000000008L, // Queen
            0x0000000000000081L, // Rook
            0x0000000000000024L, // Bishop
            0x0000000000000042L, // Knight
            0x000000000000FF00L  // Pawn
        }
        : new long[] {
            0x1000000000000000L,
            0x0800000000000000L,
            0x8100000000000000L,
            0x2400000000000000L,
            0x4200000000000000L,
            0x00FF000000000000L
        };

        for (int index = start; index < end; index++) {
            long fromBit = 1L << index;
    
            for (int type = 0; type < 6; type++) {
                if ((bot[type] & fromBit) != 0 && (initialPositions[type] & fromBit) != 0) {
                    long legalMoves = Moves.getLegalMoves(boards,type, index, _isBotWhite);
                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((legalMoves >> to) & 1) == 1 &&
                            Bot.isSafeMove(boards, type, fromBit, 1L<<to, _isBotWhite)) {
                            return BitBoard.indexToSquare(index) + BitBoard.indexToSquare(to);
                        }
                    }
                }
            }
        }
    
        return null;
    }
    
    public String findUnprotectedEnemySquare(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        long playerThreats = Moves.playerThreats(playerBoards, !_isBotWhite);
    
        long enemyCamp = _isBotWhite
            ? 0xFF00000000000000L | 0x00FF000000000000L | 0x0000FF0000000000L | 0x000000FF00000000L
            : 0x00000000000000FFL | 0x000000000000FF00L | 0x0000000000FF0000L | 0x00000000FF000000L;
    
        for (int type = 0; type < 6; type++) {
            long pieceBoard = bot[type];
    
                for (int from = 0; from < 64 && pieceBoard != 0; from++, pieceBoard >>>= 1) {
                if ((pieceBoard & 1) == 1){
                    long legal = Moves.getLegalMoves(boards, type, from, _isBotWhite);
        
                    long attackable = legal & enemyCamp & ~playerThreats;
        
                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((attackable >> to) & 1) == 1 &&
                            Bot.isSafeMove(boards, type, 1L<<from, 1L<<to, _isBotWhite)) {
                            return BitBoard.indexToSquare(from) + BitBoard.indexToSquare(to);
                        }
                    }
                }
            }
        }
    
        return null;
    }
    
    private String moveAttackedPiece(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        long playerThreat = Moves.playerThreats(playerBoards, !_isBotWhite);
    
        long noPawnsBot = 0L;
        for (int i = 0; i < 5; i++) {
            noPawnsBot |= bot[i];
        }
    
        long seriousThreats = (playerThreat & noPawnsBot);
        for (int from = 0; from < GameConsts.NUM_OF_SQUARES && seriousThreats != 0; from++, seriousThreats >>>= 1) {
            if ((seriousThreats&1) == 1) {
    
                long fromBit = 1L<<from;
                // Kings movement during threats (check) is handled on a preCalc, hence the 1-5 iteration.
                for (int type = 1; type < 6; type++) {
                    if ((bot[type] & fromBit) != 0) {
                        long legalMoves = Moves.getLegalMoves(boards,type, from, _isBotWhite);
                        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                            if (((legalMoves >> to) & 1) == 1 &&
                                Bot.isSafeMove(boards, type, fromBit, 1L<<to, _isBotWhite)) {
                                return BitBoard.indexToSquare(from) + BitBoard.indexToSquare(to);
                            }
                        }
                    }
                }
            }
        }
    
        return null;
    }
}
