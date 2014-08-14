import processing.core.PApplet;

public class Tower
{
    PApplet win2;
    // (x, 0, z) is the bottom left, furthest back corner of the tower.
    private int x;
    private int z;
    private int h;  // heights are negative, since positive y axis points downward
    private int ballsWaiting;

    public Tower(PApplet animationWindow, int originX, int originZ, int height)
    {
        win2 = animationWindow;
        x = originX;
        z = originZ;
        h = height;
        ballsWaiting = 0;
    }

    public void display()
    {
        win2.pushMatrix();
        win2.translate(x, 0, z);    // middle of bottom of tower

        win2.beginShape(win2.QUADS);
        win2.vertex(0 , 0, 0);
        win2.vertex(0, h, 0);
        win2.vertex(Animation.WALL_WID, h, 0);
        win2.vertex(Animation.WALL_WID, 0, 0);

        win2.vertex(0, 0, 0);
        win2.vertex(0, h, 0);
        win2.vertex(0, h, Animation.WALL_WID);
        win2.vertex(0, 0, Animation.WALL_WID);

        win2.vertex(0, h, Animation.WALL_WID);
        win2.vertex(0, 0, Animation.WALL_WID);
        win2.vertex(Animation.WALL_WID, 0, Animation.WALL_WID);
        win2.vertex(Animation.WALL_WID, h, Animation.WALL_WID);

        win2.vertex(Animation.WALL_WID, h, Animation.WALL_WID);
        win2.vertex(Animation.WALL_WID, 0, Animation.WALL_WID);
        win2.vertex(Animation.WALL_WID, 0, 0);
        win2.vertex(Animation.WALL_WID, h, 0);

        win2.vertex(0, 0, 0);
        win2.vertex(Animation.WALL_WID, 0, 0);
        win2.vertex(Animation.WALL_WID, 0, Animation.WALL_WID);
        win2.vertex(0, 0, Animation.WALL_WID);

        win2.vertex(0, h, 0);
        win2.vertex(Animation.WALL_WID, h, 0);
        win2.vertex(Animation.WALL_WID, h, Animation.WALL_WID);
        win2.vertex(0, h, Animation.WALL_WID);
        win2.endShape();

        if (ballsWaiting > 0)
        {
            win2.translate(Animation.WALL_WID / 2, h - Ball.RADIUS , Animation.WALL_WID / 2);     // middle of top of tower
            win2.sphere(Ball.RADIUS);
        }

        win2.popMatrix();
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }

    public int getHeight()
    {
        return h;
    }

    public String toString()
    {
        return "x: " + x + "\tz: " + z + "\th: " + h;
    }

    public void addWaitingBall()
    {
        ballsWaiting++;
    }

    public boolean takeWaitingBall()
    {
        if (ballsWaiting > 0)
        {
            ballsWaiting--;
            return true;
        }
        return false;
    }
}
