import java.util.List;
import java.util.ArrayList;
import processing.core.PApplet;

public class Animation
{
    // following constants locate and scale the animation within its window
    public static int ORIGIN_X = 200;
    public static int ORIGIN_Y = 350;
    public static int ORIGIN_Z = 0;
    public static int VOLT_SCALE = 10;
    public static int WALL_WID = 16;
    private int gridSpacing;
    private int wallLen;

    private PApplet win2;
    private Circuit circuit;
    private List<Tower> towers;
    private List<Wall> walls;
    private List<Ball> balls;
    private int numRows;
    private int numCols;

    public Animation(PApplet animationWindow, Circuit c, int terminalSpacing, int terminalRows, int terminalCols)
    {
        win2 = animationWindow;
        circuit = c;
        towers = new ArrayList<Tower>();
        walls = new ArrayList<Wall>();
        balls = new ArrayList<Ball>();
        gridSpacing = terminalSpacing;
        wallLen = gridSpacing;
        numRows = terminalRows;
        numCols = terminalCols;

        // Construct arrayList of Towers
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numCols; col++)
            {
                Terminal term = circuit.getTerminal(row, col);
                // construct tower if the terminal is connected to anything
                if (term.getConnections().size() > 0)
                {
                    towers.add(new Tower(win2, getTermX(term), getTermZ(term), getTermHeight(term)));                    
                }
            }
        }
    }

    public void displayAnimation()
    {
        win2.translate(ORIGIN_X, ORIGIN_Y, ORIGIN_Z);
        for (Tower t : towers)
        {
            t.display();
        }
    }

    private int getTermX(Terminal t)
    {
        return t.getCol() * gridSpacing;
    }

    private int getTermHeight(Terminal t)
    {
        return -1 * (int)(t.getPotential() * VOLT_SCALE);
    }

    private int getTermZ(Terminal t)
    {
        return t.getRow() * gridSpacing;
    }
}
