
package TetrisClient;

/**
 * Creates a shape object using the Tetrominoes, coordinate table, and integer table
 defines how the shape is displayed in 2D space, with functions for rotating the tetra a specific direction
 * @author Joseph Gonzales
 */
public final class Shape {
    
    private Tetrominoes tetra;
    private final int[][] coordinates;
    private int[][][] table;

    /**
     * Sets a 4x2 array of coordinates and an empty shape
     */
    public Shape() {
        coordinates = new int[4][2];
        setShape(Tetrominoes.Empty);
    }
    
    /**
     * takes in a tetromino and associates it to how the table associates it, given specific predefined coordinates
     * @param shape 
     */
    public void setShape(Tetrominoes shape) {

         table = new int[][][] {
            { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } }, // Empty shape
            { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, -1 } },// S shape
            { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } },// Z shape
            { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } },// Line shape
            { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } },// T shape
            { { 0, -1 },   { 0, 0 },   { 0, 1 },   { 1, 1 } },// J shape
            { { 1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } },// L shape
            { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 1, 0 } }// Square shape
        };

        for (int i = 0; i < 4 ; i++) {
            for (int j = 0; j < 2; ++j) {
                coordinates[i][j] = table[shape.ordinal()][i][j];
            }
        }
        tetra = shape;

    }
    /**
     * sets the x coordinate in the shape with the inputed index and 0 value
     * @param idx
     * @param x 
     */
    private void setX(int idx, int x) { 
        coordinates[idx][0] = x; 
    }
    
    /**
     * sets the y coordinate in the shape with the inputed index and 1 value
     * @param idx
     * @param y 
     */
    private void setY(int idx, int y) { 
        coordinates[idx][1] = y; 
    }
    
    /**
     * returns the y coordinate at the index
     * @param idx
     * @return 
     */
    public int getX(int idx) { 
        return coordinates[idx][0]; 
    }
    
    /**
     * returns the y coordinate at the index
     * @param idx
     * @return 
     */
    public int getY(int idx) { 
        return coordinates[idx][1]; 
    }
    
    /**
     * returns the shape of the tetromino
     * @return 
     */
    public Tetrominoes getShape()  { 
        return tetra; 
    }

    /**
     * Sets a new shape based on the input tetrominoes shape
     * @param shape
     */
    public void setNewShape(Tetrominoes shape)
    {
        setShape(shape);
    }
    
    /**
     * returns the minimum X value thats in the shape
     * @return 
     */
    public int minX()
    {
      int m = coordinates[0][0];
      for (int i=0; i < 4; i++) {
          m = Math.min(m, coordinates[i][0]);
      }
      return m;
    }

    /**
     * returns the minimum y value thats in the shape
     * @return 
     */
    public int minY() 
    {
      int m = coordinates[0][1];
      for (int i=0; i < 4; i++) {
          m = Math.min(m, coordinates[i][1]);
      }
      return m;
    }
    
    /**
    * Returns the shape after it has been mathematically rotates each square in the shape to the left
    * @return 
    */
    public Shape rotateLeft() 
    {
        //ignore square shape as it should not rotate
        if (tetra == Tetrominoes.Square) {
            return this;
        }
        //buffer new shape from modifying the old shape
        Shape result = new Shape();
        result.tetra = tetra;
        //set the new coordinates of the tetra
        for (int i = 0; i < 4; ++i) {
            result.setX(i, getY(i));
            result.setY(i, -getX(i));
        }
        return result;
    }
    
    /**
     * Returns the shape after it has been mathematically rotates each square to the right
     * @return 
     */
    public Shape rotateRight()
    {
        //ignore square shape as it should not rotate
        if (tetra == Tetrominoes.Square) {
            return this;
        }
        //buffer new shape from modifying the old shape
        Shape result = new Shape();
        result.tetra = tetra;
        //set the new coordinates of the tetra
        for (int i = 0; i < 4; ++i) {
            result.setX(i, -getY(i));
            result.setY(i, getX(i));
        }
        return result;
    }
}
