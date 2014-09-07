import processing.core.PApplet;
import java.util.List;
import java.util.ArrayList;

/**
 * Part of the View. A Wall is drawn in the 3D Animation for each component in the circuit.
 */
public class Wall
{
    private PApplet win2;
    private Tower t1;   // upstream tower
    private Tower t2;   // downstream tower
    private Tower farTower;
    private Tower nearTower;
    private double current;
    private boolean readyForBall;
    private List<Ball> balls;
    private int numBalls;
    private Wheel myWheel;

    public Wall(PApplet animationWindow, Tower upstreamTower, Tower downstreamTower, double current_)
    {
        win2 = animationWindow;
        t1 = upstreamTower;
        t2 = downstreamTower;
        // Determine which tower is far and which is near.
        farTower = t1;
        nearTower = t2;
        if (farTower.getZ() > nearTower.getZ() || farTower.getX() > nearTower.getX())
        {
            farTower = t2;
            nearTower = t1;
        }
        
        current = Math.abs(current_);
        readyForBall = false;
        balls = new ArrayList<Ball>();
        numBalls = Animation.BALLS_PER_WALL;
        myWheel = null;
    }

    public void display()
    {
        win2.fill(255);
        win2.pushMatrix();

        //translate to that tower's (x, z), but to the middle of tower instead of its corner
        win2.translate(farTower.getX() + Animation.WALL_WID, 0, farTower.getZ());

        // if necessary, rotateY(PI/2) and translate over by wallWidth
        if (farTower.getZ() != nearTower.getZ())
        {
            win2.rotateY(-1 * win2.PI/2);
            win2.translate(Animation.WALL_WID, 0, 0);
        }

        // draw wall starting at left/back tower.
        win2.beginShape(win2.QUADS);
        win2.vertex(0, 0, 0);
        win2.vertex(0, farTower.getHeight(), 0);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), 0);
        win2.vertex(Animation.WALL_LEN, 0, 0);

        win2.vertex(Animation.WALL_LEN, 0, 0);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), 0);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), Animation.WALL_WID);
        win2.vertex(Animation.WALL_LEN, 0, Animation.WALL_WID);

        win2.vertex(Animation.WALL_LEN, 0, Animation.WALL_WID);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), Animation.WALL_WID);
        win2.vertex(0, farTower.getHeight(), Animation.WALL_WID);
        win2.vertex(0, 0, Animation.WALL_WID);

        win2.vertex(0, 0, Animation.WALL_WID);
        win2.vertex(0, farTower.getHeight(), Animation.WALL_WID);
        win2.vertex(0, farTower.getHeight(), 0);
        win2.vertex(0, 0, 0);  

        win2.vertex(0, 0, 0);
        win2.vertex(Animation.WALL_LEN, 0, 0);
        win2.vertex(Animation.WALL_LEN, 0, Animation.WALL_WID);        
        win2.vertex(0, 0, Animation.WALL_WID);  

        win2.vertex(0, farTower.getHeight(), 0);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), 0);
        win2.vertex(Animation.WALL_LEN, nearTower.getHeight(), Animation.WALL_WID);        
        win2.vertex(0, farTower.getHeight(), Animation.WALL_WID);  

        win2.endShape();

        if (myWheel != null)
        {
            win2.translate(Animation.WALL_LEN / 2, (t1.getHeight() + t2.getHeight())/2, Animation.WALL_WID/2);
            myWheel.display();
            myWheel.turn();
        }

        win2.popMatrix();
    }

    public void updateBalls()
    {
        for (int i = 0; i < numBalls; i++)
        {
            Ball b = balls.get(i);
            b.display();
            b.move();
            if (b.getX() > Animation.WALL_LEN + Animation.WALL_WID)
            {
                balls.remove(b);
                numBalls--;
                readyForBall = true;
                t2.addWaitingBall();
            }
        }
        while (readyForBall && t1.takeWaitingBall())
        {
            // Find x for ball that is closest to t1
            float x = Animation.WALL_LEN + Animation.WALL_WID;
            for (Ball b : balls)
            {
                if (b.getX() < x)
                {
                    x = b.getX();
                }
            }

            addNewBall(x - (Animation.WALL_LEN + Animation.WALL_WID) / Animation.BALLS_PER_WALL);
            numBalls++;
            if (balls.size() >= Animation.BALLS_PER_WALL)
            {
                readyForBall = false;
            }
        }
    }

    public void addWheel()
    {
        int spinDirection = 1;
        if (farTower.equals(t2))
        {
            spinDirection = -1;
        }
        myWheel = new Wheel(win2, spinDirection * current);
    }

    public void addNewBall(float x)
    {
        balls.add(new Ball(win2, this, x));
    }

    public double getCurrent()
    {
        return current;
    }

    public Tower getT1()
    {
        return t1;
    }

    public Tower getT2()
    {
        return t2;
    }
}
