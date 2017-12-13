package TetrisClient;

import java.util.LinkedList;
import java.util.Random;

/**
 * This class manages a Linked List of Tetrominoes for the purpose of getting later for the game
 * It uses a Last In, First Out method of maintaining the elements in the list
 * The Tetrominoes can be popped, pushed, and peeked, as well as a size check of the list
 * @author Joseph Gonzales
 */
public class ShapeStack {
    private final LinkedList ls;
    private final Random rd;
    private final Tetrominoes[] values;
    
    /**
     * Creates a new Linked List for storing Tetrominoes
     */
    ShapeStack() {
        ls = new LinkedList();
        rd = new Random();
        values = Tetrominoes.values();
    }
    
    /**
     * Checks to see what the first element of the stack is without removing it
     * @return 
     */
    Tetrominoes peekStack() {
        return (Tetrominoes) ls.getFirst();
    }
    
    /**
     * Pushes a new Tetromino to the bottom of the stack
     * @param s
     */
    void pushStack(Tetrominoes s) {
        ls.add(s);
    }
    
    /**
     * Pushes a linked list of Tetrominoes to the stack
     * @param s 
     */
    void pushStack(LinkedList<Tetrominoes> s) {
        ls.add(s);
    }
    
    /**
     * Clears out all elements from the stack
     */
    void clearStack() {
        ls.clear();
    }
    
    /**
     * Pops the Tetromino off the stack and returns it
     * @return
     */
    Tetrominoes popStack() {
        return (Tetrominoes) ls.pop();
    }
    
    /**
     * Checks to see if the linked list has elements in it and returns the value
     * @return 
     */
    int sackSize() {
        return ls.size();
    }
    
    /**
     * Return the list of elements in the stack without removing them
     * @return 
     */
    LinkedList getList() {
        return ls;
    }
    
    /**
     * Generates a random number according to the position value of the Tetromino Enumeration
     * Pushes it onto the stack
     */
    void generateStack() {
        int x;
        for(int i = 0; i < 5; i++) {
            x = Math.abs(rd.nextInt()) % 7 + 1;//random number between 1 and 7 to generate tetromino
            pushStack(values[x]);
        }
    }
}
