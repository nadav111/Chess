package bot;

import consts.GameConsts;
import logic.BitBoard;
import logic.Moves;

public class EndingTree {
    private Node _root;
    private boolean _isBotWhite;

    public EndingTree(boolean isBotWhite) {
        _isBotWhite = isBotWhite;
        buildTree();
    }

    private void buildTree() {
        // ====== Create Answer Nodes ======
        AnswerNode ans28 = new AnswerNode(28, this::ans28);
        AnswerNode ans29 = new AnswerNode(29, this::ans29);
        AnswerNode ans43 = new AnswerNode(43, this::ans43);
        AnswerNode ans45 = new AnswerNode(45, this::ans45);
        AnswerNode ans46 = new AnswerNode(46, this::ans46);
        AnswerNode ans48 = new AnswerNode(48, this::ans48);
        AnswerNode ans49= new AnswerNode(49, this::ans49);

        // ====== Create Question Nodes ======
        QuestionNode q21 = new QuestionNode(21, this::q21);
        QuestionNode q22 = new QuestionNode(22, this::q22);
        QuestionNode q25 = new QuestionNode(25, this::q25);
        QuestionNode q40 = new QuestionNode(40, this::q40);
        QuestionNode q41 = new QuestionNode(41, this::q41);
        QuestionNode q42 = new QuestionNode(42, this::q42);
        QuestionNode q44 = new QuestionNode(44, this::q44);
        QuestionNode q47 = new QuestionNode(47, this::q47);

        // ====== חיבורים ======

        q21.setYes(q22);
        q21.setNo(q40);

        q22.setYes(ans28);
        q22.setNo(q25);

        q25.setYes(ans29);
        q25.setNo(q47);

        q40.setYes(q42);
        q40.setNo(q41);

        q42.setYes(q47);
        q42.setNo(ans45);

        q41.setYes(ans43);
        q41.setNo(q44);

        q44.setYes(ans46);
        q44.setNo(q47);

        q47.setYes(ans48);
        q47.setNo(ans49);

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
        return (answer == null)?findBestMove(boards.clone()):answer;
    }

    // ==================== Questions ====================

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

    // q22 - Checks if the bot has more points than the player. O(n^2)
    // Or if the attacker equals to the attacked piece and the matirials are equal capture
    private boolean q22(long[][] boards) {
        int trade = isGoodTrade(boards.clone());
        int materialBalance = getMaterialBalance(boards.clone());

        return trade < 0 || (trade == 0 && materialBalance >= 0);
    }
    
    // Can the attacked piece escape
    private boolean q25(long[][] boards) {
        return moveAttackedPiece(boards) != null;
    }
    
    // Checks if the bot has point advantage
    private boolean q40(long[][] boards) {
        return getMaterialBalance(boards) > 0;
    }
    // Checks if the bot's king is in the center of the board
    private boolean q41(long[][] boards) {
        return isKinginCenter(boards);
    }
    // Checks if the bot's king is in the center of the board
    private boolean q42(long[][] boards) {
        return isKinginCenter(boards);
    }
    
    // Checks if any of the bots pawns can advance (has any legal move)
    private boolean q44(long[][] boards) {
        long[] bot = boards[0];

        long pawns = bot[GameConsts.PAWN];

        for (int i = 0; i < 64; i++) {
            if (((pawns >> i) & 1) == 1) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, i, _isBotWhite);
                if (legalMoves != 0)
                    return true;
            }
        }

        return false;
    }

    // checks if there is a check on the board that the bot can make
    private boolean q47(long[][] boards) {
        return findCheckMove(boards)!=null;
    }


    // ==================== Answers ====================
    // returns the move of the capture with the max points to earn
    private String ans28(long[][] boards) {
        int[] threat = getMaxValThreatsAgainstBot(boards); // {fromIndex, toIndex}
        if (threat != null){     
            // bot's piece being attacked and then attacker's square
            String move = BitBoard.indexToSquare(threat[0]) + BitBoard.indexToSquare(threat[1]);
            return move;
        }
        return null;
    }


    // returns the move that will escape the threat on the bot's attacked piece
    private String ans29(long[][] boards) {
        return moveAttackedPiece(boards);
    }
    
    // returns the move that will make the king closer to the edge of the board
    private String ans43(long[][] boards) {
        long[] bot = boards[0];

        int kingIndex = Long.numberOfTrailingZeros(bot[GameConsts.KING]);
        long fromBit = 1L << kingIndex;
        long legal = Moves.getLegalMoves(boards, GameConsts.KING, kingIndex, _isBotWhite);

        String bestMove = null;
        double maxEdgeProximity = -1;

        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
            long toBit = 1L << to;

            if ((legal & toBit) == toBit &&
                Moves.isMoveLegal(boards, GameConsts.KING, fromBit, toBit, _isBotWhite)) {

                int row = to / 8;
                int col = to % 8;

                double proximity = Math.sqrt(Math.pow(row - 3.5, 2) + Math.pow(col - 3.5, 2));

                if (proximity > maxEdgeProximity) {
                    maxEdgeProximity = proximity;
                    bestMove = BitBoard.indexToSquare(kingIndex) + BitBoard.indexToSquare(to);
                }
            }
        }

        return bestMove;
    }

    // returns the move that will make the king closer to the center of the board
    private String ans45(long[][] boards) {
        long[] bot = boards[0];

        int kingIndex = Long.numberOfTrailingZeros(bot[GameConsts.KING]);
        long fromBit = 1L << kingIndex;
        long legal = Moves.getLegalMoves(boards, GameConsts.KING, kingIndex, _isBotWhite);

        String bestMove = null;
        double minDist = Integer.MAX_VALUE;

        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
            long toBit = 1L << to;

            if ((legal & toBit) == toBit &&
                Moves.isMoveLegal(boards, GameConsts.KING, fromBit, toBit, _isBotWhite)) {

                int row = to / 8;
                int col = to % 8;

                double proximity = Math.sqrt(Math.pow(row - 3.5, 2) + Math.pow(col - 3.5, 2));

                if (proximity < minDist) {
                    minDist = proximity;
                    bestMove = BitBoard.indexToSquare(kingIndex) + BitBoard.indexToSquare(to);
                }
            }
        }

        return bestMove;
    }

    // returns the move that will advance one pawn that is the closest to promotion
    private String ans46(long[][] boards) {
        long[] bot = boards[0];
        long pawnBoard = bot[GameConsts.PAWN];

        int bestRow = _isBotWhite ? -1 : 8;
        String bestMove = null;

        for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
            long fromBit = 1L << from;
            boolean hasPawn = (pawnBoard & fromBit) == fromBit;

            if (hasPawn) {
                long legalMoves = Moves.getLegalMoves(boards, GameConsts.PAWN, from, _isBotWhite);

                for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                    long toBit = 1L << to;
                    boolean isLegal = (legalMoves & toBit) == toBit;

                    if (isLegal && Moves.isMoveLegal(boards, GameConsts.PAWN, fromBit, toBit, _isBotWhite)) {
                        int toRow = to / 8;

                        boolean isBetter = _isBotWhite ? (toRow > bestRow) : (toRow < bestRow);

                        if (isBetter) {
                            bestRow = toRow;
                            bestMove = BitBoard.indexToSquare(from) + BitBoard.indexToSquare(to);
                        }
                    }
                }
            }
        }

        return bestMove;
    }

    // returns the move that will make the opposite king in check
    private String ans48(long[][] boards) {
        return findCheckMove(boards);
    }

    // returns the move that will make the most threat on the board
    private String ans49(long[][] boards) {
        return findBestMove(boards);
    }

    // checks if the king is in the center
    private boolean isKinginCenter(long[][] boards){
        long king = boards[0][GameConsts.KING];

        long centerMask = 
            (1L << BitBoard.squareToIndex("d4")) |
            (1L << BitBoard.squareToIndex("e4")) |
            (1L << BitBoard.squareToIndex("d5")) |
            (1L << BitBoard.squareToIndex("e5"));

        return (king & centerMask) != 0;
    }
    
    // checks if the trade is worth for the bot by value
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
                    long moves = Moves.getLegalMoves(playerBoards, playerType, from, !_isBotWhite);
                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((moves >> to) & 1) == 1) {
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

    // returns the sum of the points by value of a player
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

    // returns the move that will escape the threat on the bot's attacked piece
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
        for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
            long fromBit = 1L << from;
            if (((seriousThreats >> from)&1) == 1) {
    
                // Kings movement during threats (check) is handled on a preCalc, hence the 1-5 iteration.
                for (int type = 1; type < 6; type++) {
                    if ((bot[type] & fromBit) != 0) {
                        long legal = Moves.getLegalMoves(boards,type, from, _isBotWhite);
                        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                            long toBit = 1L << to;
                            if ((legal & toBit) != 0
                                && Bot.isSafeMove(boards, type, fromBit, toBit, _isBotWhite)) {
                                return BitBoard.indexToSquare(from) + BitBoard.indexToSquare(to);
                            }
                        }
                    }
                }
            }
        }
    
        return null;
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

        for (int playerType = 1; playerType < 6; playerType++) {
            for (int from = 0; from < 64; from++) {
                if (((player[playerType] >> from) & 1) == 1) {
                    long oppoMoves = Moves.getLegalMoves(playerBoards, playerType, from, !_isBotWhite);

                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((oppoMoves >> to) & 1) == 1) {
                            for (int botType = 1; botType < 6; botType++) {
                                if (((bot[botType] >> to) & 1) == 1) {
                                    for (int myFrom = 0; myFrom < GameConsts.NUM_OF_SQUARES; myFrom++) {
                                        if (((bot[botType] >> myFrom) & 1) == 1) {
                                            long legal = Moves.getLegalMoves(boards, botType, myFrom, _isBotWhite);

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

    // returns the move that will make the most threat on the board
    private String findBestMove(long[][] boards) {
        long[] bot = boards[0];

        String bestMove = null;
        int max = -1;
    
        for (int type = 0; type < 6; type++) {
            long pieces = bot[type];
    
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                long fromBit = 1L << from;
    
                if ((pieces & fromBit) != 0) {
                    long legal = Moves.getLegalMoves(boards,type, from, _isBotWhite);
    
                    int count = Long.bitCount(legal);
    
                    if (count > max) {
                        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                            long toBit = 1L << to;
                            if ((legal & toBit) != 0
                            && Bot.isSafeMove(boards, type, fromBit, toBit, _isBotWhite)) {
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

    // returns the check move
    private String findCheckMove(long[][] boards) {
        long[] bot = boards[0];

        for (int type = 0; type < 6; type++) {
            long pieceBoard = bot[type];

            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                long fromBit = 1L << from;
                if ((pieceBoard & fromBit) == fromBit) {
                    long legalMoves = Moves.getLegalMoves(boards, type, from, _isBotWhite);

                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        long toBit = 1L << to;

                        if ((legalMoves & toBit) == toBit &&
                            Bot.isSafeMove(boards, type, fromBit, toBit, _isBotWhite)) {

                            long[] tempPlayer = boards[0].clone();
                            long[] tempOpponent = boards[1].clone();

                            tempPlayer[type] &= ~fromBit;
                            tempPlayer[type] |= toBit;

                            for (int i = 0; i < 6; i++) {
                                tempOpponent[i] &= ~toBit;
                            }

                            long[][] newBoards = new long[][] {
                                tempOpponent,
                                tempPlayer,
                                new long[] { boards[2][0] },
                                boards[3].clone()
                            };

                            if (Moves.isInCheck(newBoards, !_isBotWhite)) {
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