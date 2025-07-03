package logic;

import bot.Bot;
import consts.GameConsts;
import graphics.ChessBoard;
import graphics.ControllerFrame;

public class GameMonitor {
    private boolean _vsAI;
    private Bot _bot;
    private boolean _isBotWhite;

    private final ChessBoard _graphicBoard;
    private final BitBoard _bitBoard;
    private ControllerFrame _controller;
    private boolean _isWhiteTurn = true;
    private boolean _promotion = false;

    public GameMonitor(boolean vsAI, boolean isBotWhite){
        _graphicBoard = new ChessBoard(this);
        _bitBoard = new BitBoard(this);
        if (vsAI)
        {
            _vsAI = vsAI;
            _isBotWhite = isBotWhite;
            
            _bot = new Bot(_isBotWhite);

            if (_isBotWhite) {
                _graphicBoard.flipBoard();
                playBot();
            }
        }
        else {
            _controller = new ControllerFrame(this);
        }
    }
    
    public void setBoard(long[][] boards) {
        _bitBoard.setBoards(boards);
        _graphicBoard.setBoards(boards);
    }

    public ChessBoard getGraphicBoard() {
        return _graphicBoard;
    }

    public BitBoard getBitBoard() {
        return _bitBoard;
    }

    public boolean handleMove(String move) {
        if (!_bitBoard.makeMove(move, _isWhiteTurn)) {
            return false;
        }

        if (!_promotion) {
            _graphicBoard.movePiece(move);
        }

        _isWhiteTurn = !_isWhiteTurn;
        _promotion = false;

        // ====== after the move happened ======
        long[][] boards = _bitBoard.getBoards(_isWhiteTurn);
        boolean isCheck = Moves.isInCheck(boards, _isWhiteTurn);
        boolean hasMove = Moves.hasAnyLegalMove(boards, _isWhiteTurn);

        if (isCheck) {
            _graphicBoard.highlightSquare(
                Long.numberOfTrailingZeros(boards[0][GameConsts.KING]) / GameConsts.NUM_OF_ROWS,
                Long.numberOfTrailingZeros(boards[0][GameConsts.KING]) % GameConsts.NUM_OF_ROWS,
                true
            );
        }

        if (!hasMove) {
            mate(isCheck); // true = mate, false = draw
        }

        if (_vsAI && _isWhiteTurn == _isBotWhite) {
            playBot();
        }

        return true;
    }

    public void showlegalMoves(String piece, int row, int col) {
        long[][] boards = _bitBoard.getBoards(_isWhiteTurn);
        int fromIndex = row * GameConsts.NUM_OF_ROWS + col;

        long moves = Moves.getLegalMoves(boards,GetPieceIndex(piece), fromIndex, _isWhiteTurn);

        for (int to = 0; to < GameConsts.NUM_OF_SQUARES; to++) {
            if (((moves >> to)&1)==1 && !Moves.isMoveLegal(boards, GetPieceIndex(piece), 1L<<fromIndex, 1L<<to, _isWhiteTurn))
            {
                moves&=~(1L<<to);
            }
        }
        for (int i = 0; i < GameConsts.NUM_OF_SQUARES; i++) {
            if (((moves >> i) & 1) == 1) {
                int destRow = i / GameConsts.NUM_OF_ROWS;
                int destCol = i % GameConsts.NUM_OF_ROWS;
                _graphicBoard.highlightSquare(destRow, destCol, false);
            }
        }
    }
    
    public int GetPieceIndex(String piece) {
        if (piece == null || piece.length() != 2)
            return -1;
    
        return switch (piece.charAt(1)) {
            case 'k' -> GameConsts.KING;
            case 'q' -> GameConsts.QUEEN;
            case 'r' -> GameConsts.ROOK;
            case 'b' -> GameConsts.BISHOP;
            case 'n' -> GameConsts.KNIGHT;
            case 'p' -> GameConsts.PAWN;
            default  -> -1;
        };
    }

    public boolean isWhiteTurn() {
        return _isWhiteTurn;
    }

    public void onCastle(String move) {
        _graphicBoard.movePiece(move);
    }

    public void onEnPassant(long board) {
        int kingIndex = Long.numberOfTrailingZeros(board); // Get position of the king
        _graphicBoard.onEnPassant(kingIndex / GameConsts.NUM_OF_ROWS, kingIndex % GameConsts.NUM_OF_ROWS);
    }
    public void check(long board) {
        int kingIndex = Long.numberOfTrailingZeros(board); // Get position of the king
        _graphicBoard.highlightSquare(kingIndex / GameConsts.NUM_OF_ROWS, kingIndex % GameConsts.NUM_OF_ROWS, true);
    }

    public String pawnPromotion(long fromBit, long toBit) {
        int srcPos = Long.numberOfTrailingZeros(fromBit); // Get position of the king
        int destPos = Long.numberOfTrailingZeros(toBit); // Get position of the king

        String piece = _graphicBoard.pawnPromotionDialog(_isWhiteTurn);
        _graphicBoard.promotePiece(srcPos/GameConsts.NUM_OF_ROWS, srcPos%GameConsts.NUM_OF_ROWS, destPos/GameConsts.NUM_OF_ROWS, destPos%GameConsts.NUM_OF_ROWS, piece);

        _promotion = true;
        return piece;
    }

    /*
     * true -> mate
     * false -> draw
     */
    void mate(boolean mate) {
        _graphicBoard.gameEnded(!_isWhiteTurn, !mate);
    }
    
    private void playBot() {
        if (_bot == null)
            _bot = new Bot(_isBotWhite);

        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            String move = _bot.playBot(_bitBoard.getBoards(_isBotWhite));
            if (move != null){
                handleMove(move);

            }
            else{
                mate(true);
            }
        });
        timer.setRepeats(false); // make the move once
        timer.start();
    }
}