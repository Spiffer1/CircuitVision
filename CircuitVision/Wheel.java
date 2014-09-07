import processing.core.PApplet;

public class Wheel
{
    float angle;
    double current;     // Current will be negative or positive to make wheel spin the right direction
    PApplet win2;
    public static int RADIUS = 12;

    public Wheel(PApplet animationWindow, double current_)
    {
        win2 = animationWindow;
        angle = 0;
        current = current_;
    }

    public void display()
    {
        win2.fill(160);
        win2.translate(0, -RADIUS, 0);
        win2.rotateY(-win2.PI/2);
        win2.rectMode(win2.CENTER);
        win2.rotateX(angle);
        win2.rect(0, 0, RADIUS, 2 * RADIUS);
        win2.rotateX(2 * win2.PI/3);
        win2.rect(0, 0, RADIUS, 2 * RADIUS);
        win2.rotateX(2 * win2.PI/3);
        win2.rect(0, 0, RADIUS, 2 * RADIUS);
    }
    
    public void turn()
    {
        angle -= Animation.SPEED * current / 10;
    }
}
