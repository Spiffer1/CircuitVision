import processing.core.PApplet;

public class Tower
{
    PApplet win2;
    // (x, 0, z) is the bottom left and farthest corner of the tower.
    private int x;  // (0, 0, 0) has already been translated translated
    private int z;
    private int h;  // heights are negative, since positive y axis points downward

    public Tower(PApplet animationWindow, int originX, int originZ, int height)
    {
        win2 = animationWindow;
        x = originX;
        z = originZ;
        h = height;
    }

    public void display()
    {
        win2.pushMatrix();
        win2.rotateX(-win2.PI / 6);
        win2.rotateY(win2.PI / 6);
        win2.translate(x, 0, z);
        
        win2.beginShape(win2.QUADS);
        win2.vertex(0, 0, 0);
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
}
