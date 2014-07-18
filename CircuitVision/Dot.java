import processing.core.PApplet;

public class Dot
{
    int row, col;
    int x, y;
    PApplet gui;

    public Dot(PApplet theGUI, int r, int c, int originX, int originY, int spacing)
    {
        gui = theGUI;
        row = r;
        col = c;
        x = originX + c * spacing;
        y = originY + r * spacing;
    }

    public void display()
    {
        gui.stroke(0);
        gui.fill(0);
        gui.ellipse(x, y, 5, 5);
    }

    // return distance between this dot and (x, y)
    public float distanceToMouse()
    {
        return (float)Math.sqrt((x - gui.mouseX) * (x - gui.mouseX) + (y - gui.mouseY) * (y - gui.mouseY));
    }
    
    public int getRow()
    {
        return row;
    }
    
    public int getCol()
    {
        return col;
    }
    
    public String toString()
    {
        return "Dot at (" + col + ", " + row + ")\n";
    }
}
