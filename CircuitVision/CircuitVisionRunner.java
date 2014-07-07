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
    private int terminalRows = 3;
    private int terminalCols = 4;
    private int gridX = 200;    // the x and y for the upper left terminal (Dot) on the screen
    private int gridY = 100;
    private int gridSpacing = 50;
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
        Circuit circuit = new Circuit(terminalRows, terminalCols);

        // Initialize dots
        for (int r = 0; r < terminalRows; r++)
        {
            for (int c = 0; c < terminalCols; c++)
            {
                dots[r][c] = new Dot(this, r, c, gridX, gridY, gridSpacing);
            }
        }

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
            //.setFont(fontMed)
        .setText("Animate Model")
        ;

        new PFrame();
    }

    public void draw()
    {
        background(150);
        drawPalette();
        drawCircuit();

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

    public void drawPalette()
    {

    }

    public void drawCircuit()
    {
        for (int r = 0; r < terminalRows; r++)
        {
            for (int c = 0; c < terminalCols; c++)
            {
                dots[r][c].display();
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
