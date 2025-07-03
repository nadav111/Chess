package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import consts.GameConsts;

public class Square extends JPanel{
    private int _row;
    private int _col;
    private Img _pieceImage;
    private String _piece;
    private boolean _isHighlighted = false;
    private boolean _isCheck = false;
    private Color _defaultBackground;
    private ChessBoard _listener;
    

    public Square(int row, int col){
        _row = row;
        _col = col;
        if ((row + col) % 2 == 0) {
            _defaultBackground = GameConsts.BOARD_WHITE;
        } else {
            _defaultBackground = GameConsts.BOARD_BLACK;
        }
        setBackground(_defaultBackground);
        

        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e) {
                _listener.squareSelected(_row, _col);
            }
        });
    }

    public void setPiece(String piece) {
        _piece = piece;

        String imagePath = getImagePathForPiece(piece);
        if (imagePath != null) {
            _pieceImage = new Img(imagePath);
        }

        repaint();   
    }
    
    public void removePiece() {
        _piece = null;
        _pieceImage = null;
        repaint();
    }
    private String getImagePathForPiece(String piece) {
        switch (piece) {
            case "bp": return "img/b_pawn.png";  // Black Pawn
            case "br": return "img/b_rook.png";  // Black Rook
            case "bn": return "img/b_knight.png";  // Black Knight
            case "bb": return "img/b_bishop.png";  // Black Bishop
            case "bq": return "img/b_queen.png";  // Black Queen
            case "bk": return "img/b_king.png";  // Black King
            case "wp": return "img/w_pawn.png";  // White Pawn
            case "wr": return "img/w_rook.png";  // White Rook
            case "wn": return "img/w_knight.png";  // White Knight
            case "wb": return "img/w_bishop.png";  // White Bishop
            case "wq": return "img/w_queen.png";  // White Queen
            case "wk": return "img/w_king.png";  // White King

            default: return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        int squareWidth = getWidth();
        int squareHeight = getHeight();
    
        if (_pieceImage != null) {
            int imageSize = Math.min(squareWidth, squareHeight);
            imageSize-=10;
            int imageX = (squareWidth - imageSize) / 2; // Center the image horizontally
            int imageY = (squareHeight - imageSize) / 2; // Center the image vertically
    
            _pieceImage.setImgCords(imageX, imageY);
            _pieceImage.setImgSize(imageSize, imageSize);
    
            _pieceImage.drawImg(g);
        }

        if (_isHighlighted) {
            if (_isCheck){
                setBackground(Color.red);
            }
            else{
                setBackground(Color.yellow);
            }
        }
        else{
            setBackground(_defaultBackground);
        }
    }

    public String getPiece() {
        return _piece;
    }

    
    public void setListener(ChessBoard _listener) {
        this._listener = _listener;
    }

    public void setIsHighlighted(boolean _isHighlighted, boolean isCheck) {
        this._isCheck = isCheck;
        this._isHighlighted = _isHighlighted;
        repaint();
    }
}
