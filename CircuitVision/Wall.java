import processing.core.PApplet;

/**
 * Part of the View. A Wall is drawn in the 3D Animation for each component in the circuit.
 */
public class Wall
{
    private Component c;
    private PApplet win2;
    
    public Wall(PApplet animationWindow, Component comp)
    {
        win2 = animationWindow;
        c = comp;
    }
    
    public void display()
    {
        win2.fill(255);
        win2.pushMatrix();
        
        //translate
    }
}
