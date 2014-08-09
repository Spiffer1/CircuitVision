import java.util.List;
import java.util.ArrayList;
import processing.core.PApplet;

public class Animation
{
    // following constants locate and scale the animation within its window
    public static int ORIGIN_X = 200;
    public static int ORIGIN_Y = 200;
    public static int ORIGIN_Z = 0;
    public static int VOLT_SCALE = 10;
    public static int WALL_WID = 16;
    public static int WALL_LEN;     // defaults to gridSpacing - WALL_WID
    
    private int gridSpacing;
    private PApplet win2;
    private Circuit circuit;
    private List<Tower> towers;
    private List<Wall> walls;
    private List<Ball> balls;
    private int numRows;
    private int numCols;

    public Animation(PApplet animationWindow, Circuit circ, int terminalSpacing, int terminalRows, int terminalCols)
    {
        win2 = animationWindow;
        circuit = circ;
        towers = new ArrayList<Tower>();
        walls = new ArrayList<Wall>();
        balls = new ArrayList<Ball>();
        gridSpacing = terminalSpacing;
        WALL_LEN = gridSpacing - WALL_WID;
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

        // Construct arrayList of Walls
        for (Component c : circuit.getComponents())
        {
            // Set downstream end of component to term2 and upstream end to term1
            Terminal term1 = c.getEndPt1();
            Terminal term2 = c.getEndPt2();
            double current = c.getCurrent();
            if (term1.equals(c.getCurrentDirection()) && current > 0 || term2.equals(c.getCurrentDirection()) && current < 0)
            {
                term1 = c.getEndPt2();
                term2 = c.getEndPt1();
            }
            walls.add(new Wall(win2, findTowerAtLocation(term1.getRow(), term1.getCol()), findTowerAtLocation(term2.getRow(), term2.getCol()), current));
        }
    }

    public void displayAnimation()
    {
        win2.translate(ORIGIN_X, ORIGIN_Y, ORIGIN_Z);
        for (Tower t : towers)
        {
            t.display();
        }
        for (Wall w : walls)
        {
            w.display();
        }
    }

    private Tower findTowerAtLocation(int row, int col)
    {
        for (Tower tower : towers)
        {
            if (tower.getX() == col * gridSpacing && tower.getZ() == row * gridSpacing)
            {
                return tower;
            }
        }
        return null;
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
