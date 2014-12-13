/*
 * This Runner allows the user to add components to a grid of terminals
 * by clicking on a component (resistor, wire, or battery) in a palette and then clicking
 * between two dots in the grid of terminals.
 * 
 * Once a complete circuit has been constructed, the user can click the "Animate Model"
 * button and an animated 3D model of the circuit will display in a second window.
 * 
 * Code written by Sean Fottrell
 * November 14, 2014
 * 
 * Concept based on CircuitVision by Benjamin Lai, circa 1992
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
    private boolean windowLocationSet;
    private boolean newAnimation;
    private boolean animating;
    private boolean showValues;
    private int circuitMode;    // 1: add resistor; 2: add wire; 3: add battery; 4: remove component; 0: no mode selected
    private boolean scaleVolts;     // sets autoscaling by a toggle in PreferencesApplet
    private int voltScale;
    private boolean rotationEnabled; 

    // "Animate Model" button coordinates and "Show Values" button coordinates
    private int animLeft, animRight, animTop, animBottom;
    private int showValLeft, showValRight, showValTop, showValBottom;

    // to make a second window...
    private SecondApplet win2;
    // to make a Preferences Window...
    private Frame prefsFrame;
    private PreferencesApplet prefs;
    private boolean showPrefs;  // makes Preferences window visible
    private boolean prevShowValue;

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

        windowLocationSet = false;
        newAnimation = false;
        animating = false;
        showValues = false;
        showPrefs = false;
        circuitMode = 0;
        rotationEnabled = false;
        voltScale = 10;

        prefs = addPreferencesFrame();

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

        /* ***************************************************************************
         * 
         * Add ControlP5 buttons to the circuit window
         * 
         *****************************************************************************/
        cp5 = new ControlP5(this);
        // Add buttons
        showValLeft = 20;
        showValTop = 320;
        showValRight = showValLeft + 80;   // width = 80
        showValBottom = showValTop + 30;   // height = 30
        cp5.addToggle("showValues")
        .setPosition(showValLeft, showValTop)
        .setSize(showValRight - showValLeft, showValBottom - showValTop)
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

        cp5.addToggle("showPrefs")  // Clicking this button will toggle the value of the global boolean variable, showPrefs
        .setPosition(35, 10)
        .setSize(50, 25)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Prefs")
        ;

        cp5.addToggle("resistorMode")
        .setPosition(20, 70)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Resistor")
        ;

        cp5.addToggle("wireMode")
        .setPosition(20, 120)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Wire")
        ;

        cp5.addToggle("batteryMode")
        .setPosition(20, 170)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Add Battery")
        ;

        cp5.addToggle("removeMode")
        .setPosition(20, 220)
        .setSize(80, 30)
        .getCaptionLabel()
        .align(ControlP5.CENTER, ControlP5.CENTER)
        .setText("Remove Component")
        ;

        //Create a default circuit for testing. This can be eliminated to start with a blank grid.
        circuit.addBattery(new Battery(6), 1, 0, 2, 0, 1, 0);  // Extra two arguments set the positive end of the battery.
        circuit.addComponent(new Wire(), 1, 0, 1, 1);
        circuit.addComponent(new Resistor(3), 1, 1, 2, 1);
        circuit.addComponent(new Wire(), 2, 1, 2, 0);
        //         circuit.addComponent(new Resistor(5), 1, 1, 1, 2);
        // 
        //         circuit.addComponent(new Wire(), 1, 2, 2, 2);
        //         circuit.addComponent(new Resistor(4), 2, 2, 2, 1);
        // 
        //         circuit.addComponent(new Resistor(8), 2, 2, 2, 3);   // a dead-end
        //         circuit.addComponent(new Wire(), 2, 3, 1, 3);
        //         circuit.addComponent(new Battery(4), 1, 2, 1, 3);   // defaults to making the first coordinates the pos. end

        //circuit.addComponent(new Wire(), 1, 1, 1, 2);
        //circuit.addComponent(new Wire(), 3, 0, 2, 0);

        new PFrame();
    }

    public void draw()
    {
        if (!windowLocationSet)
        {
            frame.setLocation(550, 0);
            windowLocationSet = true;
        }
        background(150);
        drawCircuit();
    }

    public PreferencesApplet addPreferencesFrame()
    {
        int width = 300;
        int height = 300;
        prefsFrame = new Frame("Preferences");
        PreferencesApplet prefApp = new PreferencesApplet(width, height);
        prefsFrame.add(prefApp);
        prefApp.init();
        prefsFrame.setTitle("Preferences");
        prefsFrame.setSize(width, height);
        prefsFrame.setLocation(0, 450);
        prefsFrame.setVisible(showPrefs);
        return prefApp;
    }

    // If mouse clicked in circuit area, add (or remove) component
    public void mouseClicked()
    {
        int mX = mouseX;
        int mY = mouseY;
        if ( !((mX > animLeft && mX < animRight && mY > animTop && mY < animBottom)
            || (mX > showValLeft && mX < showValRight && mY > showValTop && mY < showValBottom)) ) // not on animate button or Show Values button
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
                    int r = c.getResistance();
                    String input = JOptionPane.showInputDialog("Enter Resistance in ohms:", Integer.toString(r));
                    if (input != null)
                    {
                        try
                        {
                            r = Integer.parseInt(input);
                        }
                        catch (NumberFormatException e)
                        {
                            // does nothing, so resistance is unchanged
                        }
                        c.setResistance (r);
                    }
                }
                if (c instanceof Battery)
                {
                    if (minDist2 - minDist < gridSpacing / 3)   // If you click near the middle of the battery...
                    {
                        // ...joptionpane prompts for new voltage value
                        double v = ((Battery)c).getVoltage();
                        String input = JOptionPane.showInputDialog("Enter Voltage in Volts:", Double.toString(v));
                        if (input != null)
                        {
                            try
                            {
                                v = Double.parseDouble(input);
                            }
                            catch (NumberFormatException e)
                            {
                                // does nothing, so Voltage stays as it is
                            }
                            ((Battery)c).setVoltage(v);
                        }
                    }
                    else    // If you click near the end of the battery...
                    {
                        ((Battery)c).setPosEnd(circuit.getTerminal(r1, c1));    // ...that end becomes the positive terminal
                    }
                }
            }
            else if (c == null)
            {
                if (circuitMode == 1)
                {
                    // (Maybe) Add ControlP5 text field beside or above resistor
                    circuit.addComponent(new Resistor(10), r1, c1, r2, c2);
                }
                if (circuitMode == 2)
                {
                    circuit.addComponent(new Wire(), r1, c1, r2, c2);
                }
                if (circuitMode == 3)
                {
                    circuit.addBattery(new Battery(6), r1, c1, r2, c2, r1, c1);  // pos end is dot closest to click 
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
            ((Toggle)cp5.getController("showValues")).setState(false);
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
            ((Toggle)cp5.getController("showValues")).setState(false);
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
            ((Toggle)cp5.getController("showValues")).setState(false);
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
            ((Toggle)cp5.getController("showValues")).setState(false);
        }
    }

    public void showValues(boolean on)
    {
        if (on)
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
                showValues = true;
            }
        }
        else
        {
            showValues = false;
        }
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
            animating = true;
        }
    }

    public void drawCircuit()
    {
        // Draw terminals
        for (int row = 0; row < terminalRows; row++)
        {
            for (int col = 0; col < terminalCols; col++)
            {
                if (showValues)
                {
                    textAlign(LEFT);
                }
                dots[row][col].display(circuit, showValues);
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
                stroke(0);
                line(x1, y1, x2, y2);
            }
            else if (c instanceof Resistor)
            {
                if (y1 == y2) // horizontal resistor
                {
                    int startX = Math.min(x1, x2) + (gridSpacing - 26) / 2;
                    // Display Resistance
                    textAlign(CENTER);
                    textSize(12);
                    fill(0);
                    text(c.getResistance(), startX + 13, y1 - 10);

                    // Draw resistor
                    stroke(0);
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
                    textAlign(RIGHT);
                    textSize(12);
                    fill(0);
                    text(c.getResistance(), x1 - 8, startY + 17);
                    stroke(0);
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

                    textAlign(CENTER);
                    textSize(12);
                    fill(0);
                    text( Double.toString( ((Battery)c).getVoltage() ), 0, -12 );

                    if (Math.min(c.getEndPt1().getCol(), c.getEndPt2().getCol()) == ((Battery)c).getPosEnd().getCol())
                    {
                        rotate(PI);
                    }
                }
                else // vertical battery
                {
                    int y0 = Math.min(y1, y2);
                    translate(x1, y0 + gridSpacing / 2);

                    textAlign(RIGHT);
                    textSize(12);
                    fill(0);
                    text( Double.toString( ((Battery)c).getVoltage() ), -11, 4 );

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
                stroke(0);
                line(-gridSpacing / 2, 0, -3, 0);
                line(3, 0, gridSpacing / 2, 0);
                line(-3, -4, -3, 4);
                line(3, -8, 3, 8);
                popMatrix();
            }
            // Show current
            if (showValues && Math.abs(c.getCurrent()) > .00000001)
            {
                boolean end1Arrow = true;     // arrow end closer to EndPoint1
                int biggerEnd2 = 1;   // = -1 if x1 > x2
                int left = 1;   // if left = -1: arrow points right/down; if left = 1, arrow points left/up
                if (c.getCurrentDirection().equals(c.getEndPt2()))
                {
                    end1Arrow = !end1Arrow;
                    left *= -1;
                }
                if (x1 > x2 || y1 > y2)
                {
                    biggerEnd2 *= -1;
                    left *=-1;
                }
                if (c.getCurrent() < 0)
                {
                    end1Arrow = !end1Arrow;
                    left *= -1;
                }
                stroke(255);
                fill(255);
                textSize(10);
                double current = Math.abs(c.getCurrent());
                current = (int)(current * 100 + 0.5) / 100.0;
                if (y1 == y2)   // a horizontal component
                {
                    // Draw current arrow
                    line(x1 + biggerEnd2 * 25, y1 + 10, x2 - biggerEnd2 * 25, y1 + 10);
                    if (end1Arrow)
                    {
                        line(x1 + left * 25, y1 + 10, x1 + left * 28, y1 + 13);
                        line(x1 + left * 25, y1 + 10, x1 + left * 28, y1 + 7);
                    }
                    else
                    {
                        line(x2 + left * 25, y1 + 10, x2 + left * 28, y1 + 13);
                        line(x2 + left * 25, y1 + 10, x2 + left * 28, y1 + 7);
                    }
                    // Display number of amps
                    textAlign(CENTER);
                    text( Double.toString(current) + " A", (x1 + x2) / 2, y1 + 23 );
                }
                else    // a vertical component
                {
                    // Draw current arrow
                    line(x1 + 10, y1 + biggerEnd2 * 25, x1 + 10, y2 - biggerEnd2 * 25);
                    if (end1Arrow)
                    {
                        line(x1 + 10, y1 + left * 25, x1 + 7, y1 + left * 28);
                        line(x1 + 10, y1 + left * 25, x1 + 13, y1 + left * 28);                        
                    }
                    else
                    {
                        line(x2 + 10, y2 + left * 25, x2 + 7, y2 + left * 28);
                        line(x2 + 10, y2 + left * 25, x2 + 13, y2 + left * 28);
                    }
                    // Display number of amps
                    textAlign(LEFT);
                    text( Double.toString(current) + " A", x1 + 13, (y1 + y2) / 2 + 5 );
                }
            }
        }
    }

    int win2width = 500;
    public static int win2height = 400;

    public class PFrame extends Frame 
    {
        public PFrame() 
        {
            setBounds(0, 0, win2width, win2height);
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
                    anim = new Animation(this, circuit, gridSpacing, terminalRows, terminalCols, scaleVolts, voltScale, rotationEnabled);
                    voltScale = anim.VOLT_SCALE;
                    newAnimation = false;
                }
                ortho();
                background(100);
                fill(255);
                anim.displayAnimation();
                redraw();
            }
        }
    }

    public class PreferencesApplet extends PApplet
    {
        private int w;
        private int h;
        private ControlP5 prefsCp5;
        Textfield voltScaleField;

        public PreferencesApplet(int width, int height)
        {
            w = width;
            h = height;
            scaleVolts = false;
        }

        public void setup()
        {
            size(w, h);
            frameRate(25);
            prefsCp5 = new ControlP5(this);

            prefsCp5.addToggle("autoScaleVolts")
            .setPosition(20, 50)
            .setSize(120, 30)
            .getCaptionLabel()
            .align(ControlP5.CENTER, ControlP5.CENTER)
            .setText("Autoscale Voltage")
            ;

            prefsCp5.addToggle("enableRotation")
            .setPosition(20, 90)
            .setSize(120, 30)
            .setValue(rotationEnabled)
            .getCaptionLabel()
            .align(ControlP5.CENTER, ControlP5.CENTER)
            .setText("Enable 3D Rotation")
            ;

            voltScaleField = prefsCp5.addTextfield("vScale")
            .setPosition(180, 50)
            .setSize(30, 20)
            .setAutoClear(false)
            .setValue(Integer.toString(voltScale))
            ;
        }

        public void draw()
        {
            if (showPrefs != prevShowValue)
            {
                prefsFrame.setVisible(showPrefs);
                prevShowValue = showPrefs;
            }
            if (scaleVolts && !Integer.toString(voltScale).equals(voltScaleField.getText()))
            {
                voltScaleField.setValue(Integer.toString(voltScale));
            }
        }

        public void autoScaleVolts(boolean on)
        {
            if (on)
            {
                scaleVolts = true;
            }
            else
            {
                scaleVolts = false;
            }
            newAnimation = true;
        }

        public void enableRotation(boolean on)
        {
            if (on)
            {
                rotationEnabled = true;
            }
            else
            {
                rotationEnabled = false;
            }
            newAnimation = true;
        }      

        public void vScale(String volts)
        {
            try 
            {
                voltScale = Integer.parseInt(volts);
                newAnimation = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Volt Scale must be an integer.");
            }
        }
    }
}
