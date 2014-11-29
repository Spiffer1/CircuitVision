import java.util.List;
import java.util.ArrayList;
import processing.core.PApplet;

/**
 * Activated by CircuitVisionRunner: When the AnimateModel button is clicked, 
 * the circuit is solved and the "animating" and "newAnimation" flags are set 
 * to true. The latter flag results in an object of type Animation be instantiated.
 * 
 * The Animation object's job is primarily to create instances of "Wall"s, "Tower"s, "Ball"s,
 * and "Wheel"s.
 */
public class Animation
{
    // following constants locate and scale the animation within its window
    public static int ORIGIN_X; // set within displayAnimation() to keep model centered
    public static int ORIGIN_Y; // set within displayAnimation()
    public static int ORIGIN_Z = -100;
    public static int VOLT_SCALE = 10;
    public static int WALL_WID = 16;
    public static int WALL_LEN;     // defaults to gridSpacing - WALL_WID
    public static int BALLS_PER_WALL = 3;
    public static float SPEED = (float).5; // scale factor for ball speed and water wheel speed

    private boolean autoScale;
    private int gridSpacing;
    private PApplet win2;
    private Circuit circuit;
    private List<Tower> towers;
    private List<Wall> walls;
    private int numRows;
    private int numCols;

    public Animation(PApplet animationWindow, Circuit circ, int terminalSpacing, int terminalRows, int terminalCols)
    {
        win2 = animationWindow;
        circuit = circ;
        towers = new ArrayList<Tower>();
        walls = new ArrayList<Wall>();
        gridSpacing = terminalSpacing;
        WALL_LEN = gridSpacing - WALL_WID;
        numRows = terminalRows;
        numCols = terminalCols;
        autoScale = true;

        // Scale the Animation display
        if (autoScale)
        {
            double maxPotential = 0;
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numCols; col++)
                {
                    Terminal term = circuit.getTerminal(row, col);
                    double v = term.getPotential();
                    if (v < Double.MAX_VALUE && v > maxPotential)
                    {
                        maxPotential = term.getPotential();
                    }
                }
            }
            double maxBattVolts = 0;
            for (Component c : circuit.getComponents())
            {
                if (c instanceof Battery)
                {
                    if ( ((Battery)c).getVoltage() > maxBattVolts)
                    {
                        maxBattVolts = ((Battery)c).getVoltage();
                    }
                }
            }
            VOLT_SCALE = (int)(CircuitVisionRunner.win2height / (2 * maxPotential));
            VOLT_SCALE = (int)Math.min(VOLT_SCALE, 100 / maxBattVolts);
        }

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

        // Construct balls on each wall
        for (Wall wall : walls)
        {
            for (int i = 0; i < BALLS_PER_WALL; i++)
            {
                wall.addNewBall(i * (WALL_LEN + WALL_WID) / BALLS_PER_WALL);
            }
        }

        // Construct water wheels for each resistor and SkiLift for each battery
        for (Component c : circuit.getComponents())
        {
            if (c instanceof Resistor || c instanceof Battery)
            {
                // Find corresponding wall
                Tower t1 = findTowerAtLocation(c.getEndPt1().getRow(), c.getEndPt1().getCol());
                Tower t2 = findTowerAtLocation(c.getEndPt2().getRow(), c.getEndPt2().getCol());
                for (Wall w : walls)
                {
                    if (w.getT1().equals(t1) && w.getT2().equals(t2) || w.getT2().equals(t1) && w.getT1().equals(t2))
                    {
                        if (c instanceof Resistor)
                        {
                            w.addWheel();
                        }
                        else
                        {
                            Tower posEnd = findTowerAtLocation( ((Battery)c).getPosEnd().getRow(), ((Battery)c).getPosEnd().getCol() );
                            w.addSkiLift(posEnd);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void displayAnimation()
    {
        ORIGIN_X = win2.width / 4;
        ORIGIN_Y = win2.height / 2;
        win2.translate(ORIGIN_X, ORIGIN_Y, ORIGIN_Z);
        win2.rotateX(-win2.PI / 6);

        // Enable rotation of the animation by moving mouse over its window
        win2.translate((int)(gridSpacing * 1.5), 0, (int)(gridSpacing * 1.5));
        win2.rotateY(win2.map(win2.mouseX, 0, win2.width, -win2.PI*(float)2.2, -win2.PI/6));
        win2.translate(-(int)(gridSpacing * 1.5), 0, -(int)(gridSpacing * 1.5));

        //win2.rotateY(-win2.PI / 6);   // standard viewing angle if not using mouse rotation (above)

        for (Tower t : towers)
        {
            t.display();
        }
        for (Wall w : walls)
        {
            w.display();
            w.updateBalls();
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
