import processing.core.PApplet;

public class Ball
{
    PApplet win2;
    private float x;
    private Wall myWall;
    private int speed = 2; // scale factor for ball speed
    private int radius = 4;

    public Ball(PApplet animationWindow, Wall wall, int initX)
    {
        win2 = animationWindow;
        myWall = wall;
        x = initX;
    }

    public void move()
    {
        x += (float)(speed * myWall.getCurrent());
        if (x > Animation.WALL_LEN + Animation.WALL_WID)
        {
            //x = 0;
            x = x % (Animation.WALL_LEN + Animation.WALL_WID);
        }
    }

    public void display()
    {
        win2.pushMatrix();
        // translate to coordinates of center of top of t1
        int height;
        int h1 = myWall.getT1().getHeight();
        int h2 = myWall.getT2().getHeight();
        if (x < Animation.WALL_WID / 2)
        {
            height = h1;
        }
        else if (x > Animation.WALL_LEN + Animation.WALL_WID / 2)
        {
            height = h2;
        }
        else
        {
            height = h1 + (int)((h2 - h1) * (x - Animation.WALL_WID/2.0) / Animation.WALL_LEN); 
        }
        win2.translate(myWall.getT1().getX() + Animation.WALL_WID/2, height, myWall.getT1().getZ() + Animation.WALL_WID/2);
        
        // rotate so current is running left to right.
        if (myWall.getT1().getZ() > myWall.getT2().getZ())
        {
            win2.rotateY(win2.PI / 2);
        }
        else if (myWall.getT1().getX() > myWall.getT2().getX())
        {
            win2.rotateY(win2.PI);
        }
        else if (myWall.getT1().getZ() < myWall.getT2().getZ())
        {
            win2.rotateY(3 * win2.PI / 2);            
        }
        // draw sphere at appropriate height (tower or wall height)
        win2.translate(x, -radius, 0);
        //System.out.println(x);
        win2.sphere(radius);
        
        win2.popMatrix();
    }
}

