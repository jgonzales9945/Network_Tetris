package TetrisClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Sets up a tetris board that defines how pieces are drawn on the board and the conditions for rotating/moving each TPiece.
 * Gets new pieces from a stack that this and the thread maintaining the outbound key event stack calls to generate.
 * If the game ends, set a condition for the thread to use while ending the game
 * @author Joseph Gonzales
 */
public class Board extends JPanel implements ActionListener {


    public final int BoardWidth = 10;
    public final int BoardHeight = 22;

    public Timer time;
    public boolean isFallingFinished = false;
    public boolean isStarted = false;
    public boolean isPaused = false;
    public boolean isConcluded = false;
    public boolean pieceSend = false;
    public int LinesRemoved = 0;
    public int DX = 0;
    public int DY = 0;
    public Shape TPiece;
    public Tetrominoes[] board;
    public ShapeStack tetStk;
    public LinkedList<Integer> kpStk;
    
    /**
     * Creates a board with the parent and integer that specifies how the board will be controlled
 initializes the key event stack and tetromino stack, generates pieces for the stack, 
 initializes TPiece, defines how the time works and starts it
     * @param parent
     * @param i 
     */
    public Board(TetrisC parent, int i) {

        setFocusable(true);
        tetStk = new ShapeStack();
        kpStk = new LinkedList<>();
        if(i != 1) { 
            addKeyListener(new ActionKey(this)); //0 adapts keyboard control, otherwise the parent thread handles the internal functions
        }

        TPiece = new Shape();
        time = new Timer(400, this);
        time.start(); 

        board = new Tetrominoes[BoardWidth * BoardHeight];
        clearBoard();  
    }
    
    /**
     * Checks if the TPiece is finished falling, else moves the TPiece down one line
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            moveOneDown();
        }
    }

    /**
     * returns the width of the tetromino block in relation to the size of the window
     * @return 
     */
    int squareWidth() { 
        return (int) getSize().getWidth() / BoardWidth; 
    }
    
    /**
     * returns the height of the tetromino block in relation to the size of the window
     * @return 
     */
    int squareHeight() { 
        return (int) getSize().getHeight() / BoardHeight; 
    }
    
    /**
     * returns the position of the shape at parameter x and y
     * @param x
     * @param y
     * @return 
     */
    Tetrominoes shapePosition(int x, int y) { 
        return board[(y * BoardWidth) + x]; 
    }

    /**
     * Start method that is called by the parent
 sets all the global conditions, calls the new TPiece function, and starts the time
     * @param i
     */
    public void start(int i)
    {
        if (isPaused) {
            return;
        }
        
        isStarted = true;
        isFallingFinished = false;
        LinesRemoved = 0;
        clearBoard();
        if(i != 0) tetStk.generateStack();

        newPiece();
        time.start();
    }

    /**
     * Pauses the time and sets the value of isPaused 
     */
    public void pause()
    {
        if (!isStarted)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            time.stop();
        } else {
            time.start();
        }
        repaint();
    }
    
    /**
     * draws the current tetrominoes on the board as well as the current TPiece falling
     * @param g 
     */
    @Override
    public void paint(Graphics g)
    { 
        super.paint(g);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();


        for (int i = 0; i < BoardHeight; ++i) {
            for (int j = 0; j < BoardWidth; ++j) {
                Tetrominoes shape = shapePosition(j, BoardHeight - i - 1);
                if (shape != Tetrominoes.Empty)
                    drawSquare(g, 0 + j * squareWidth(), boardTop + i * squareHeight(), shape);
            }
        }

        if (TPiece.getShape() != Tetrominoes.Empty) {
            for (int i = 0; i < 4; ++i) {
                int x = DX + TPiece.getX(i);
                int y = DY - TPiece.getY(i);
                drawSquare(g, 0 + x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(), TPiece.getShape());
            }
        }
    }
    
    /**
     * Checks to see if the TPiece can be moved all the way down to the bottom of the board in one instant
 This is for dropping a TPiece to a position that the player knows will immediately fit
     */
    public void dropToBottom()
    {
        int y = DY;
        while (y > 0) {
            if (!tryToMove(TPiece, DX, y - 1)) {
                break;
            }
            --y;
        }
        pieceDropped();
    }
    
    /**
     * Drops the TPiece one line down
     */
    public void moveOneDown()
    {
        if (!tryToMove(TPiece, DX, DY - 1)) {
            pieceDropped();
        }
    }

    /**
     * Clears the board with empty pieces 
     */
    private void clearBoard()
    {
        for (int i = 0; i < BoardHeight * BoardWidth; ++i) {
            board[i] = Tetrominoes.Empty;
        }
    }
    
    /**
     * Defines how the TPiece being dropped will work, then calls removeFullLines function to remove lines on the board
 then calls a new TPiece if the current TPiece is done falling
     */
    public void pieceDropped()
    {
        for (int i = 0; i < 4; ++i) {
            int x = DX + TPiece.getX(i);
            int y = DY - TPiece.getY(i);
            board[(y * BoardWidth) + x] = TPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }
    
    /**
     * Gets a new TPiece by popping it off the stack
 If no TPiece is available, generate more pieces on the stack
 Set the DX, DY to the new position where the blocks should be on the top of the board
 if the TPiece collides with a tetromino that is too high on the board, then pause the time declare the game over
     */
    public void newPiece()
    {
        while(true) {
            if(tetStk.sackSize() > 1) {
                TPiece.setNewShape(tetStk.popStack());
                break;
            }
            else {
               tetStk.generateStack();
            }
        }
        DX = BoardWidth / 2 + 1;
        DY = BoardHeight - 1 + TPiece.minY();

        if (!tryToMove(TPiece, DX, DY)) {
            TPiece.setShape(Tetrominoes.Empty);
            time.stop();
            isStarted = false;
        }
    }
    
    /**
     * Try to move the tetromino TPiece based on the newX and newY parameters
 Also make sure the TPiece does not exceed the board size boundaries
 set new TPiece, DX, DY equal to the shape and position relative to the board
 call to repaint the board
     * @param newPiece
     * @param newX
     * @param newY
     * @return 
     */
    public boolean tryToMove(Shape newPiece, int newX, int newY)
    {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.getX(i);
            int y = newY - newPiece.getY(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)//acknowledge boundaries
                return false;
            if (shapePosition(x, y) != Tetrominoes.Empty)
                return false;
        }

        TPiece = newPiece;
        DX = newX;
        DY = newY;
        repaint();
        return true;
    }
    
    /**
     * Checks to see if a row on the board is full of squares, sets the condition that the line is or isn't full
     * then check to see if the line is in fact full, clears it, then shifts the remaining pieces down
     */
    public void removeFullLines()
    {
        int numFullLines = 0;
        
        //check every row for complete lines
        for (int i = BoardHeight - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BoardWidth; ++j) {
                if (shapePosition(j, i) == Tetrominoes.Empty) {
                    lineIsFull = false; //don't clear line since it is not full
                    break;//go clear line
                }
            }

            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BoardHeight - 1; ++k) {
                    for (int j = 0; j < BoardWidth; ++j)
                         board[(k * BoardWidth) + j] = shapePosition(j, k + 1);//move down the line above the cleared line
                }
            }
        }

        if (numFullLines > 0) {
            LinesRemoved += numFullLines;
            isFallingFinished = true;
            TPiece.setShape(Tetrominoes.Empty);
            repaint();
        }
     }
    /**
     * Generates colors based on which tetromino is which, with no shape being set to black
 sets the graphics to reflect that of the rectangles of each TPiece
     * @param g
     * @param x
     * @param y
     * @param shape 
     */
    public void drawSquare(Graphics g, int x, int y, Tetrominoes shape)
    {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), 
            new Color(102, 204, 102), new Color(102, 102, 204), 
            new Color(204, 204, 102), new Color(204, 102, 204), 
            new Color(102, 204, 204), new Color(218, 170, 0)
        };


        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + 1);
    }   
}