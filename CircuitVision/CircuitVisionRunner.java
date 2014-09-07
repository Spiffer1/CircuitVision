/*
 * This Runner allows the user to add components to a grid of terminals
 * by clicking on a component (resistor, wire, or battery) in a palette and then clicking
 * between two dots in the grid of terminals.
 * 
 * Once a complete circuit has been constructed, the user can click the "Animate Model"
 * button and an animated 3D model of the circuit will display in a second window.
 * 
 * This version is a first pass at the GUI. The "Show Info" button is not yet
 * implemented.
 * 
 * by Sean Fottrell
 * September 1, 2014
 */

import javax.swing.JOptionPane;
import java.awt.*;
import processing.core.*;   // library for the Processing language, developed by Casey Reas and Ben Fry
                            // See processing.org for more info
import controlP5.*; // libary for making buttons (and more); see www.sojamo.de/libraries/controlP5

/**
 * CircuitVisionRunner is the controller class for the CircuitVision program. This class controls and displays
 * the main GUI window containing the circuit diagram and buttons that allow interaction with the program.
 * 
 * Other classes in the View are "Dot", representing the terminals in the circuit diagram, and a set of 
 * classes for displaying an additional window (win2) for the 3-D animation: "Animation", "Wall", "Tower", 
 * "Ball", and "Wheel".
 * 
 * This class and the "Animation" class use a set of Model classes, that represent the circuit components
 * and solve the circuit to find currents and potentials at each point. These classes include: "Circuit",
 * "Component" (with its sub-classes "Resistor", "Battery", and "Wire") and "Terminal".
 */
public class CircuitVisionRunner extends PApplet
{
    private Circuit circuit;    // holds the circuit model
    private static int terminalRows = 4;
    private static int terminalCols = 4;
    private static int gridX = 200;    // the x and y for the upper left terminal (Dot) on the screen
    private static int gridY = 100;
    private static int gridSpacing = 80;

    private ControlP5 cp5;
    private boolean newAnimation;
    private boolean animating;
    private boolean showValues;
    private int circuitMode;    // 1: add resistor; 2: add wire; 3: add battery; 4: remove component; 0: no mode selected

    // "Animate Model" button coordinates
    private int animLeft, animRight, animTop, animBottom;

    // to make a second window...
    private SecondApplet win2;

    // make 2D array of Dot objects: the terminals shown on the screen
    private Dot[][] dots = new Dot[terminalRows][terminalCols];

    public static void main(String args[]) 
    {
        PApplet.main(new String[] { "CircuitVisionRunner" });
    }

    public void setup()
    {
        size(600,400);
        smooth();

        newAnimation = false;
        animating = false;
        showValues = false;
        circuitMode = 0;

        // make new Circuit object
        circuit = new Circuit(terminalRows, terminalCols);

        // Initialize dots
        for (int r = 0; r < terminalRows; r++)
        {
            for (int c = 0; c < terminalCols; c++)
            {
                dots[r][c] = new Dot(this, r, c, gridX, gridY, gridSpacing);
            }
        }

        // Add buttons
        cp5 = new ControlP5(this);
        cp5.addBang("showValues")
        .setPosition(20, 320)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
            //.setFont(fontMed)
        .setText("Show Values")
        ;

        animLeft = 20;
        animTop = 360;
        animRight = animLeft + 80;  // width = 80
        animBottom = animTop + 30; // height = 30
        cp5.addBang("animateModel")
        .setPosition(animLeft, animTop)
        .setSize(animRight - animLeft, animBottom - animTop)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Animate Model")
        ;

        cp5.addToggle("resistorMode")
        .setPosition(20, 50)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Resistor")
        ;

        cp5.addToggle("wireMode")
        .setPosition(20, 100)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Wire")
        ;

        cp5.addToggle("batteryMode")
        .setPosition(20, 150)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Battery")
        ;

        cp5.addToggle("removeMode")
        .setPosition(20, 200)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Remove Component")
        ;

        // Create a default circuit for testing. This can be eliminated to start with a blank grid.
        circuit.addBattery(new Battery(6), 1, 0, 2, 0, 1, 0);  // Extra two arguments set the positive end of the battery.
        circuit.addComponent(new Wire(), 1, 0, 1, 1);
        circuit.addComponent(new Resistor(3), 1, 1, 2, 1);
        circuit.addComponent(new Resistor(9), 1, 1, 2, 1);  // Test adding component where one already exists (shouldn't add it)
        circuit.addComponent(new Wire(), 2, 1, 2, 0);
        //circuit.addComponent(new Resistor(5), 1, 1, 1, 2);

        circuit.addComponent(new Wire(), 1, 2, 2, 2);
        circuit.addComponent(new Resistor(4), 2, 2, 2, 1);

        circuit.addComponent(new Resistor(8), 2, 2, 2, 3);   // a dead-end
        circuit.addComponent(new Wire(), 2, 3, 1, 3);
        circuit.addComponent(new Battery(4), 1, 2, 1, 3);   // defaults to making the first coordinates the pos. end

        new PFrame();
    }

    public void draw()
    {
        background(150);
        drawCircuit();
    }

    // If mouse clicked in circuit area, add (or remove) component
    public void mouseClicked()
    {
        int mX = mouseX;
        int mY = mouseY;
        if ( !(mX > animLeft && mX < animRight && mY > animTop && mY < animBottom) ) // not on animate button
        {
            animating = false;
        }
        // Determine two terminals (row and column) that click was between
        Dot closest = dots[0][0];
        Dot nextClosest = dots[0][0];
        float minDist = gridSpacing;
        float minDist2 = gridSpacing;
        for (int r = 0; r < terminalRows; r++)
        {
            for (int c = 0; c < terminalCols; c++)
            {
                float dist = dots[r][c].distanceToMouse();
                if (dist < minDist)
                {
                    nextClosest = closest;
                    minDist2 = minDist;
                    closest = dots[r][c];
                    minDist = dist;
                }
                else if (dist < minDist2)
                {
                    nextClosest = dots[r][c];
                    minDist2 = dist;
                }                
            }
        }
        int r1 = closest.getRow();
        int c1 = closest.getCol();
        int r2 = nextClosest.getRow();
        int c2 = nextClosest.getCol();
        // Add component to circuit model
        if (minDist < gridSpacing && minDist2 < gridSpacing)
        {
            // get component between those terminals (null if none)
            Component c = circuit.getComponent(r1, c1, r2, c2);
            if (c != null && circuitMode == 4)
            {
                circuit.removeComponent(c);
            }
            else if (c != null)
            {
                if (c instanceof Resistor)
                {
                    // joption pane to get resistance
                }
                if (c instanceof Battery)
                {
                    // joptionpane to get voltage
                }
            }
            else if (c == null)
            {
                if (circuitMode == 1)
                {
                    circuit.addComponent(new Resistor(10), r1, c1, r2, c2);
                }
                if (circuitMode == 2)
                {
                    circuit.addComponent(new Wire(), r1, c1, r2, c2);
                }
                if (circuitMode == 3)
                {
                    circuit.addBattery(new Battery(12), r1, c1, r2, c2, r1, c1);  // pos end is dot closest to click 
                }
            }
        }
    }

    public void resistorMode(boolean on)
    {
        if (on)
        {
            circuitMode = 1;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void wireMode(boolean on)
    {
        if (on)
        {
            circuitMode = 2;
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void batteryMode(boolean on)
    {
        if (on)
        {
            circuitMode = 3;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void removeMode(boolean on)
    {
        if (on)
        {
            circuitMode = 4;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
        }
    }

    public void showValues()
    {
        circuitMode = 0;
        ((Toggle)cp5.getController("wireMode")).setState(false);
        ((Toggle)cp5.getController("resistorMode")).setState(false);
        ((Toggle)cp5.getController("batteryMode")).setState(false);
        ((Toggle)cp5.getController("removeMode")).setState(false);
        showValues = true;
    }

    public void animateModel()
    {
        circuitMode = 0;
        ((Toggle)cp5.getController("wireMode")).setState(false);
        ((Toggle)cp5.getController("resistorMode")).setState(false);
        ((Toggle)cp5.getController("batteryMode")).setState(false);
        ((Toggle)cp5.getController("removeMode")).setState(false);
        double[] currents = circuit.solve();
        if (currents == null)
        {
            JOptionPane.showMessageDialog(null, "Short Circuit or Incomplete Circuit", "WARNING", JOptionPane.WARNING_MESSAGE);
        }
        else 
        {
            newAnimation = true;
        }
        animating = true;

    }

    public void drawCircuit()
    {
        // Draw terminals
        for (int row = 0; row < terminalRows; row++)
        {
            for (int col = 0; col < terminalCols; col++)
            {
                dots[row][col].display();
            }
        }
        // Draw Components
        for (Component c : circuit.getComponents())
        {
            int x1 = gridX + c.getEndPt1().getCol() * gridSpacing;
            int y1 = gridY + c.getEndPt1().getRow() * gridSpacing;
            int x2 = gridX + c.getEndPt2().getCol() * gridSpacing;
            int y2 = gridY + c.getEndPt2().getRow() * gridSpacing;
            if (c instanceof Wire)
            {
                line(x1, y1, x2, y2);
            }
            else if (c instanceof Resistor)
            {
                if (y1 == y2) // horizontal resistor
                {
                    int startX = Math.min(x1, x2) + (gridSpacing - 26) / 2;
                    line(startX, y1, startX + 3, y1 - 5);
                    line(startX + 3, y1 - 5, startX + 8, y1 + 5);
                    line(startX + 8, y1 + 5, startX + 13, y1 - 5);
                    line(startX + 13, y1 - 5, startX + 18, y1 + 5);
                    line(startX + 18, y1 + 5, startX + 23, y1 - 5);
                    line(startX + 23, y1 - 5, startX + 26, y1);
                    line(Math.min(x1, x2), y1, startX, y1);
                    line(Math.max(x1, x2), y1, startX + 26, y1);
                }
                else  // vertical resistor
                {
                    int startY = Math.min(y1, y2) + (gridSpacing - 26) / 2;
                    line(x1, startY, x1 - 5, startY + 3);
                    line(x1 - 5, startY + 3, x1 + 5, startY + 8);
                    line(x1 + 5, startY + 8, x1 - 5, startY + 13);
                    line(x1 - 5, startY + 13, x1 + 5, startY + 18);
                    line(x1 + 5, startY + 18, x1 - 5, startY + 23);
                    line(x1 - 5, startY + 23, x1, startY + 26);
                    line(x1, Math.min(y1, y2), x1, startY);
                    line(x1, Math.max(y1, y2), x1, startY + 26);                    
                }
            }
            else if (c instanceof Battery)
            {
                pushMatrix();
                // translate to middle of battery and rotate to get pos end on right
                if (y1 == y2) // horizontal battery
                {
                    int x0 = Math.min(x1, x2);
                    translate(x0 + gridSpacing / 2, y1);
                    if (Math.min(c.getEndPt1().getCol(), c.getEndPt2().getCol()) == ((Battery)c).getPosEnd().getCol())
                    {
                        rotate(PI);
                    }
                }
                else // vertical battery
                {
                    int y0 = Math.min(y1, y2);
                    translate(x1, y0 + gridSpacing / 2);
                    if (Math.min(c.getEndPt1().getRow(), c.getEndPt2().getRow()) == ((Battery)c).getPosEnd().getRow())
                    {
                        rotate(-PI / 2);
                    }
                    else
                    {
                        rotate(PI / 2);
                    }
                }
                // draw battery
                line(-gridSpacing / 2, 0, -3, 0);
                line(3, 0, gridSpacing / 2, 0);
                line(-3, -4, -3, 4);
                line(3, -8, 3, 8);
                popMatrix();
            }
        }
    }

    int win2width = 600;
    int win2height = 400;
    public class PFrame extends Frame 
    {
        public PFrame() 
        {
            setBounds(win2height, win2height, win2width, win2height);
            win2 = new SecondApplet();
            add(win2);
            win2.init();
            setVisible(true); // was show();
        }
    }

    public class SecondApplet extends PApplet
    {
        private Animation anim;

        public void setup()
        {
            size(win2width, win2height, P3D);
            ortho();
            lights();
            stroke(0);
            frameRate(30);
            sphereDetail(6);
        }

        public void draw()
        {
            if (animating)
            {
                if (newAnimation)
                {
                    anim = new Animation(this, circuit, gridSpacing, terminalRows, terminalCols);
                    newAnimation = false;
                }
                background(100);
                fill(255);
                anim.displayAnimation();
                redraw();
            }
        }
    }
}
