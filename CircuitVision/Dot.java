import processing.core.PApplet;

/**
 * This class is part of the View. Each terminal in the circuit schematic is represented
 * by a Dot. The number of rows and columns of Dots is set within the CircuitVisionRunner.
 */
public class Dot
{
    private int row, col;
    private int x, y;   // Pixel coordinates
    private PApplet gui;

    /**
     * Constructor for a new Dot, which corresponds to one terminal in the Circuit.
     * @param theGUI reference to the Processing graphics window which holds the circuit diagram.
     * @param r  Row number of the Dot
     * @param c  Column number of the Dot
     * @param originX  X coordinate of the upper left dot in the matrix
     * @param originY  Y coordinate of the upper left dot in the matrix
     * @param spacing  Number of pixels between each pair of dots
     */
    public Dot(PApplet theGUI, int r, int c, int originX, int originY, int spacing)
    {
        gui = theGUI;
        row = r;
        col = c;
        x = originX + c * spacing;
        y = originY + r * spacing;
    }

    /**
     * Sends calls methods from the Processing library to display a dot. Defaults to a black circle
     * with 5 pixel diameter.
     */
    public void display()
    {
        gui.stroke(0);
        gui.fill(0);
        gui.ellipse(x, y, 5, 5);
    }

    /**
     * @return distance in pixels from this dot to the last mouse click.
     */ 
    public float distanceToMouse()
    {
        return (float)Math.sqrt((x - gui.mouseX) * (x - gui.mouseX) + (y - gui.mouseY) * (y - gui.mouseY));
    }

    /**
     * @return  This dot's row.
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return  This dot's column.
     */
    public int getCol()
    {
        return col;
    }

    /**
     * @return Returns a string with the dot location expressed as an ordered pair (x, y).
     */
    public String toString()
    {
        return "Dot at (" + col + ", " + row + ")\n";
    }
}
