package bot;

import consts.GameConsts;
import logic.BitBoard;
import logic.Moves;

public class Bot {
    private boolean _isBotWhite;
    private OpeningTree _opening;
    private MiddleTree _middle;
    private EndingTree _end;
    private int _phase;
    private int _moveCount;

    public Bot(boolean isBotWhite){
        _moveCount = 0;
        _isBotWhite = isBotWhite;
        _opening = new OpeningTree(isBotWhite);
        _phase = 1;
    }
    
    public String playBot(long[][] board) {
        if(_moveCount==12){
            _phase=2;
            System.out.println("====================== PHASE 2 ======================");
        }
        
        if (getTotalPieces(board) <= 13)
        {
            _phase=3;
            System.out.println("====================== PHASE 3 ======================");
        }

        String move;
        move = preCalcs(board);
        if (move != null) {
            _moveCount+=1;
            return move;
        }
        
        if (_phase == 1)
        {
            move = _opening.run(board);
            if (move != null && move.equals("DONE"))
                _phase=2;
        }
        if (_phase == 2)
        {
            if (_middle == null) {
                _middle = new MiddleTree(_isBotWhite);
            }
            move = _middle.run(board);
        } else if (_phase == 3){
            if(_end == null) {
                _end = new EndingTree(_isBotWhite);
            }
            move = _end.run(board);
        }
        _moveCount+=1;
        return move;
    }

    private String preCalcs(long[][] boards) {
        if (Moves.isInCheck(boards, _isBotWhite)) {
            String escapeMove = tryEscapeCheck(boards.clone());
            if (escapeMove != null) {
                System.out.println("Bot is in check — escaping with: " + escapeMove);
                return escapeMove;
            }
        }
    
        String move = findMateInOne(boards.clone());

        if (move != null)
            return move;

        move = captureHangedPiece(boards);

        if (move != null)
            return move;

        return move;
    }

    private String findMateInOne(long[][] boards) {
        long[] botPieces = boards[0];

        for (int type = 0; type < 6; type++) {
            long pieceboard = botPieces[type];
    
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                if (((pieceboard >> from) & 1) == 1){
                    long legalMoves = Moves.getLegalMoves(boards, type, from, _isBotWhite);

                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        if (((legalMoves >> to) & 1) == 1 && isMate(boards.clone(), type, from, to)) {
                            return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isMate(long[][] boards, int type, int fromIndex, int toIndex) {
        long[] tempBot = boards[0].clone();
        long[] tempPlayer = boards[1].clone();
    
        long fromBit = 1L << fromIndex;
        long toBit = 1L << toIndex;
    
        tempBot[type] &= ~fromBit;
        tempBot[type] |= toBit;
    
        for (int i = 0; i < 6; i++) {
            tempPlayer[i] &= ~toBit;
        }
    
        long[][] newBoards = new long[][] {
            tempPlayer,
            tempBot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };
    

        boolean isCheck = Moves.isInCheck(newBoards, !_isBotWhite);
        if (isCheck) {
            System.out.println("Check found by bot");
            boolean hasMoves = Moves.hasAnyLegalMove(newBoards, !_isBotWhite);
            if (hasMoves) System.out.println("Opponent still has legal moves");
            else System.out.println("Mate confirmed");
            return !hasMoves;
        }
        return false;
    }
    
    
    private String captureHangedPiece(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] opponentBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };
    
        long botThreats = Moves.playerThreats(boards, _isBotWhite);
        long playerDefense = Moves.playerThreats(opponentBoards, !_isBotWhite);
    
        for (int type = 0; type < 6; type++) {
            long enemyPieces = player[type];
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                long fromBit = 1L << from;
    
                boolean isExposed = ((enemyPieces & fromBit) != 0)
                    && ((botThreats & fromBit) != 0)
                    && ((playerDefense & fromBit) == 0);
    
                if (isExposed) {
                    for (int botType = 0; botType < 6; botType++) {
                        long botPieces = bot[botType];
                        for (int botFrom = 0; botFrom < GameConsts.NUM_OF_SQUARES; botFrom++) {
                            long botFromBit = 1L << botFrom;
                            if ((botPieces & botFromBit) != 0) {
                                long legal = Moves.getLegalMoves(boards, botType, botFrom, _isBotWhite);
                                if ((legal & fromBit) != 0 &&
                                    Bot.isSafeMove(boards, botType, botFromBit, fromBit, _isBotWhite)) {
                                    return BitBoard.indexToSquare(botFrom) + BitBoard.indexToSquare(from);
                                }
                            }
                        }
                    }
                }
            }
        }
    
        return null;
    }
    

    // O(n^6)
    private String tryEscapeCheck(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];
    
        long[][] playerBoards = new long[][] {
            player,
            bot,
            new long[] { boards[2][0] },
            boards[3].clone()
        };

        int kingIndex = Long.numberOfTrailingZeros(bot[GameConsts.KING]);

        // Try to capture the checker. The last piece to try and capture is the king to minimize king's movement.
        for (int type = 0; type < 6; type++) {
            long attackers = player[type];

            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                if (((attackers >> from) & 1) == 1) {
                    long moves = Moves.getLegalMoves(playerBoards,type, from, !_isBotWhite);
        
                    if (((moves >> kingIndex) & 1) == 1) {
                        // so the king capture will be the last
                        for (int botType = 5; botType >= 0; botType--) {
                            long pieceboard = bot[botType];

                            for (int src = 0; src < GameConsts.NUM_OF_SQUARES; src++) {
                                if (((pieceboard >> src) & 1) == 1) {
                                    long legalMoves = Moves.getLegalMoves(boards,botType, src, _isBotWhite);

                                    if (((legalMoves >> from)&1) == 1 &&
                                        Moves.isMoveLegal(boards, botType, 1L<<src, 1L<<from, _isBotWhite)) {
                                        return BitBoard.convertToMoveString(src / 8, src % 8, from / 8, from % 8);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // last resort. try every move to escape mate.
        for (int type = 0; type < 6; type++) {
            long pieceboard = bot[type];
            for (int from = 0; from < GameConsts.NUM_OF_SQUARES; from++) {
                long fromBit = 1L << from;
        
                if (((pieceboard & fromBit) != 0)) {
                    long legalMoves = Moves.getLegalMoves(boards, type, from, _isBotWhite);
        
                    for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
                        long toBit = 1L << to;
                        boolean isLegalTarget = ((legalMoves & toBit) != 0);
        
                        if (isLegalTarget) {
                            boolean moveLegal = Moves.isMoveLegal(boards, type, fromBit, toBit, _isBotWhite);
                            if (moveLegal) {
                                return BitBoard.convertToMoveString(from / 8, from % 8, to / 8, to % 8);
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public static boolean isSafeMove(long[][] boards, int pieceType, long fromBit, long toBit, boolean isBotWhite) {
        if (!Moves.isMoveLegal(boards, pieceType, fromBit, toBit, isBotWhite)) {
            return false;
        }

        // Simulate the move
        long[] bot = boards[0].clone();
        long[] player = boards[1].clone();

        // Move piece
        bot[pieceType] &= ~fromBit;
        bot[pieceType] |= toBit;

        // Remove opponent piece if captured
        for (int i = 0; i < 6; i++) {
            player[i] &= ~toBit;
        }

        // Rebuild simulated board
        long[][] simulatedBoards = new long[][] {
            player,
            bot,
            boards[2].clone(),
            boards[3].clone()
        };

        // Check if toBit is now under opponent threat (after the sim)
        long opponentThreats = Moves.playerThreats(simulatedBoards, !isBotWhite);

        // BitBoard.printLegalMoves(opponentThreats);
        // BitBoard.printLegalMoves(toBit);

        return (opponentThreats & toBit) == 0;
    }
    
    private int getTotalPieces(long[][] boards) {
        long[] bot = boards[0];
        long[] player = boards[1];

        int total = 0;

        for (int i = 0; i < 6; i++) {
            total += Long.bitCount(bot[i]);
            total += Long.bitCount(player[i]);
        }

        return total;
    }
}