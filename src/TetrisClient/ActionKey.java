
package TetrisClient;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Sets up a Key listener that reads in keyboard inputs and translates them to game logic
 * Also sets the key event to the respective boards buffer, to be used by the out bound data thread
 * @author Joseph Gonzales
 */
public class ActionKey implements KeyListener {

    private Board t;
    
    /**
     * Default constructor
     */
    ActionKey() {
        
    }
    
    /**
     * Sets this Action listener to know about the conditions of the board
     * @param t 
     */
    ActionKey(Board t) {
        this.t = t;
    }
    
    /**
     * unused
     * @param kt 
     */
    @Override
    public void keyTyped(KeyEvent kt) {
        
    }
    
    /**
     * unused
     * @param kr 
     */
    @Override
    public void keyReleased(KeyEvent kr) {
        
    }

    /**
     * Listens for key events, except if the game is paused or no pieces are available.
     * Copies the key event object to the instance boards linked list for use there.
     * Then calls the specified function based on the input given by the key code.
     * @param kp 
     */
    @Override
    public void keyPressed(KeyEvent kp) {

        if(!this.t.isStarted || this.t.TPiece.getShape() == Tetrominoes.Empty) {
            return;
        }

        int keycode = kp.getKeyCode();
        this.t.kpStk.add(keycode);// pushes key event to the board stack

        if (this.t.isPaused){
            return;// ignore keys if game is halted
        }
        
        switch (keycode) {
        case KeyEvent.VK_LEFT:// move TPiece left
            this.t.tryToMove(this.t.TPiece, this.t.DX - 1, t.DY);
            break;
        case KeyEvent.VK_RIGHT:// move TPiece right
            this.t.tryToMove(this.t.TPiece, this.t.DX + 1, this.t.DY);
            break;
        case KeyEvent.VK_Z:// rotate TPiece to the right by calling its method for TPiece rotation
            this.t.tryToMove(this.t.TPiece.rotateRight(), this.t.DX, this.t.DY);
            break;
        case KeyEvent.VK_X:// rotate TPiece to the left by calling its method for TPiece rotation
            this.t.tryToMove(this.t.TPiece.rotateLeft(), this.t.DX, this.t.DY);
            break;
        case KeyEvent.VK_UP:// drop TPiece all the way to the bottom of the board or where the pieces are instantly
            this.t.dropToBottom();
            break;
        case KeyEvent.VK_DOWN:// drop TPiece down one line
            this.t.moveOneDown();
            break;
        }

    }
}