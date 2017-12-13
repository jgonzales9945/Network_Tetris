
package TetrisClient;

/**
 * Lists all of the possible shapes of Tetrominoes
 * @author Joseph Gonzales
 */
public enum Tetrominoes { 
    Empty, // no shape
    S, // left up to right
    Z, // right down to left
    Line, // long
    T, // vertical to middle
    J, // bottom to top right
    L, // bottom right to top
    Square // box
};
