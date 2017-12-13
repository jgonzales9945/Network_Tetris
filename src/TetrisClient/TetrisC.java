package TetrisClient;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Sets up the game
 * @author Joseph Gonzales
 */
public class TetrisC extends JFrame {
	
    private Board board1, board2;
    
    private OutputManager manager1;
    private InputManager manager2;
    
    /**
     *Default constructor
     */
    public TetrisC() {
        
    }
    /**
     * Creates new form TetrisClient that holds both Tetris boards with a specific size
     * Sets up a window event to send a forfeit message and closes the game
     * Creates two I/O management Objects that monitor the boards stacks and modifies how the second board behaves
     * @param c
     * @param inp
     * @param outp
     */
    public TetrisC(Socket c, DataInputStream inp, DataOutputStream outp) {
        
        this.setLayout(new GridLayout(1,2));
        this.setLocationRelativeTo(null);

        this.setSize(400, 400);
        this.setTitle("Tetris");
        
        this.board1 = new Board(this,0);//player board
        this.board2 = new Board(this,1);//networked player board
        this.board1.setSize(200, 400);//one half
        this.board2.setSize(200, 400);//other half
        this.add(board1);
        this.add(board2);
        
        board1.start(0);
        board2.start(1);
        this.manager1 = new OutputManager(board1, outp);
        this.manager2 = new InputManager(board2, board1, inp);
        
        this.addWindowListener(new WindowAdapter() {
            /**
             * Sets window event to declare a forfeit to the opponent before disconnecting and ending the game 
             * @param we 
             */
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    outp.writeShort(3);//declare forfeit
                    outp.flush();
                    c.close();
                } catch (IOException e) {
                }
                System.exit(0);
            }
         } );
        
        this.setVisible(true);
        startThreads();// initiating I/O network management
    }
    /**
     * start network I/O management threads
     */
    private void startThreads() {
        manager1.start();
        manager2.start();
    }
}
/**
 * Manages the out bound data stream to the server using the current board and it's variables
 * Sends a short to indicate what type of data or notification to the server
 * Then sets up an object output stream object to write the tetromino stack or key event to the network
 * Displays a message to the client if they lost and exits out of the thread
 * @author Joseph Gonzales
 */
class OutputManager extends Thread {
    Board t;
    DataOutputStream out;
    ObjectOutputStream output;
    
    /**
     * Set variables
     * @param t
     * @param outp 
     */
    OutputManager(Board t, DataOutputStream outp) {
        this.t = t;
        this.out = outp;
    }
    
    /**
     * Loops until the boards conclusion variable is not false
     * checks to see if the player lost, then sends a message to the server that indicating a loss, while notifying the player of their loss
     * sets up an object output stream to write the boards linked list and keycode to the network
     */
    @Override
    public void run() {
        try {
            this.output = new ObjectOutputStream(this.out);
            while(this.t.time.isRunning()){
                
                if(this.t.kpStk.size() > 0) {
                    out.writeShort(0);
                    out.flush();
                    out.writeInt(t.kpStk.remove());
                    out.flush();
                    System.out.println("sent key code");
                }
                if(this.t.tetStk.sackSize() >= 7) {
                    out.writeShort(1);
                    out.flush();
                    output.writeObject(t.tetStk.getList());
                    output.flush();
                    System.out.println("sent stack");
                }/*
                if(this.t.tetStk.sackSize() < 3) {
                    t.tetStk.generateStack();// generate more tetrominoes if the stack runs low
                    System.out.println("generate stack");
                }*/
            }
            if(this.t.isStarted == false) {
                this.t.time.stop();
                out.writeShort(2);
                out.flush();
                JOptionPane.showMessageDialog(null, "Lost", "You Lost!\nNow exiting", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            ex.getStackTrace();
        }
    }
}
/**
 * Checks the input stream from the server and reads in data based on what the server sent
 * appends pieces to the network players board stack
 * handles the key event object by directly translating it to movements exactly as the local player would
 * if the opponent loses or forfeits, stop the game and notify the player that they won
 * @author Joseph Gonzales
 */
class InputManager extends Thread {
    Board t1, t2;
    DataInputStream in;
    
    /**
     * sets up the instances of both boards
     * @param t2
     * @param t1
     * @param inp 
     */
    InputManager(Board t2, Board t1, DataInputStream inp) {
        this.t1 = t1;
        this.t2 = t2;
        this.in = inp;
        this.t2.tetStk.clearStack();
    }
    
    /**
     * Read in the short that the server sent, then read in the object, such as the key event or linked list
     * If the object was a key event object, process it for the second board
     * If the server sends 2, then stop the game and tell the player they won
     * If the server sends 3, then stop the game and tell the player that the opponent forfeited
     */
    @Override
    public void run() {
        while(t1.time.isRunning()) {
            try {
                short i = in.readShort();
                ObjectInputStream input;
                switch(i) {
                    case 0:
                        keyProcess(in.readInt());
                        System.out.println("input received");
                        break;
                    case 1:
                        input = new ObjectInputStream(in);
                        LinkedList<Tetrominoes> t = (LinkedList<Tetrominoes>) input.readObject();
                        this.t2.tetStk.pushStack(t);
                        System.out.println("Stack received");
                        break;
                    case 2:
                        this.t1.isConcluded = true;
                        this.t1.isPaused = true;
                        if(t1.isStarted == true) JOptionPane.showMessageDialog(null, "Win", "You Won!\nNow exiting", JOptionPane.INFORMATION_MESSAGE);
                        this.t1.time.stop();
                        this.t2.time.stop();
                        System.exit(0);
                    case 3:
                        this.t1.isConcluded = true;
                        this.t1.isPaused = true;
                        JOptionPane.showMessageDialog(null, "Forfeit", "Opponent has Forfeit!\nNow exiting", JOptionPane.INFORMATION_MESSAGE);
                        this.t1.time.stop();
                        this.t2.time.stop();
                        System.exit(0);
                        
                    default: break;
                }
            } 
            catch (IOException | ClassNotFoundException ex) {
            }
        }
        JOptionPane.showMessageDialog(null, "Disconnection", "The connection has been Disconnected\nNow exiting", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
    
    /**
     * Translates the integer key code received to act on the networked player board exactly as it would for the normal player
     * @param kp 
     */
    public void keyProcess(int kp) {
        if (!this.t2.isStarted || this.t2.TPiece.getShape() == Tetrominoes.Empty) {
            return;
        }

        if (this.t2.isPaused){
            return;//ignore keys if game is halted
        }
        
        switch (kp) {
        case 37:// move TPiece left
            this.t2.tryToMove(this.t2.TPiece, this.t2.DX - 1, this.t2.DY);
            break;
        case 39:// move TPiece right
            this.t2.tryToMove(this.t2.TPiece, this.t2.DX + 1, this.t2.DY);
            break;
        case 90:// rotate TPiece to the right by calling its method for TPiece rotation
            this.t2.tryToMove(this.t2.TPiece.rotateRight(), this.t2.DX, this.t2.DY);
            break;
        case 88:// rotate TPiece to the left by calling its method for TPiece rotation
            this.t2.tryToMove(this.t2.TPiece.rotateLeft(), this.t2.DX, this.t2.DY);
            break;
        case 38:// drop TPiece all the way to the bottom of the board or where the pieces are instantly
            this.t2.dropToBottom();
            break;
        case 40:// drop TPiece down one line
            this.t2.moveOneDown();
            break;
        }
    }
}