/*
 * This Runner will eventually allow the user to add components to a grid of terminals
 * by clicking on a component (resistor, wire, or battery) in a palette and then clicking
 * between two dots in the grid of terminals.
 * 
 * Once a complete circuit has been constructed, the user can click the "Animate Model"
 * button and an animated 3D model of the circuit will display in a second window.
 * 
 * This version is a proof of concept for the GUI. It does not actually allow create
 * new components and the animation is not of a circuit. But it does allow control of 
 * two windows and buttons.
 * 
 * by Sean Fottrell
 * July 6, 2014
 */

import java.awt.*;
import processing.core.*;
import controlP5.*; // libary for making buttons (and more)

public class CircuitVisionRunner extends PApplet
{
    private Circuit circuit;
    private int terminalRows = 4;
    private int terminalCols = 4;
    private int gridX = 200;    // the x and y for the upper left terminal (Dot) on the screen
    private int gridY = 100;
    private int gridSpacing = 80;
    ControlP5 cp5;
    boolean animating;
    boolean showValues;

    // to make a second window...
    private SecondApplet win2;

    // make 2D array of Dot objects: the terminals shown on the screen
    Dot[][] dots = new Dot[terminalRows][terminalCols];

    public static void main(String args[]) 
    {
        PApplet.main(new String[] { "CircuitVisionRunner" });
    }

    public void setup()
    {
        size(600,400);
        smooth();

        animating = false;
        showValues = false;

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

        cp5.addBang("animateModel")
        .setPosition(20, 360)
        .setSize(80, 30)
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

        // Until GUI can add components, use these:
        circuit.addBattery(new Battery(6), 1, 0, 2, 0, 1, 0);  // Extra two arguments set the positive end of the battery.
        circuit.addComponent(new Wire(), 1, 0, 1, 1);
        circuit.addComponent(new Resistor(3), 1, 1, 2, 1);
        circuit.addComponent(new Resistor(9), 1, 1, 2, 1);  // Test adding component where one already exists (shouldn't add it)
        circuit.addComponent(new Wire(), 2, 1, 2, 0);
        circuit.addComponent(new Resistor(5), 1, 1, 1, 2);
        
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

        // if (paletteclicked) animating = false; update cursor with component

        // if (circuitclicked) animating = false; add component to circuit

    }

    public void resistorMode(boolean on)
    {
        if (on)
        {
            animating = false;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void wireMode(boolean on)
    {
        if (on)
        {
            animating = false;
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void batteryMode(boolean on)
    {
        if (on)
        {
            animating = false;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("removeMode")).setState(false);
        }
    }

    public void removeMode(boolean on)
    {
        if (on)
        {
            animating = false;
            ((Toggle)cp5.getController("wireMode")).setState(false);
            ((Toggle)cp5.getController("resistorMode")).setState(false);
            ((Toggle)cp5.getController("batteryMode")).setState(false);
        }
    }

    public void showValues()
    {
        animating = false;
        showValues = true;
    }

    public void animateModel()
    {
        animating = true;
    }

    // If mouse clicked in circuit area, add (or remove) component
    public void mouseClicked()
    {

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

    public class PFrame extends Frame 
    {
        public PFrame() 
        {
            setBounds(300, 300, 400, 300);
            win2 = new SecondApplet();
            add(win2);
            win2.init();
            setVisible(true); // was show();
        }
    }

    public class SecondApplet extends PApplet
    {
        public void setup()
        {
            size(400, 300, P3D);
        }

        public void draw()
        {
            if (animating)
            {
                background(100);
                fill(255);
                translate(200, 150, 0);
                rotateY(frameCount/(float)30.0);
                box(15, 100, 150);

                redraw();
            }
        }
    }
}
