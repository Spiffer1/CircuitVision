import processing.core.PApplet;

/**
 * Part of the View. A Wall is drawn in the 3D Animation for each component in the circuit.
 */
public class Wall
{
    private PApplet win2;
    private Tower t1;
    private Tower t2;
    private double current;

    public Wall(PApplet animationWindow, Tower upstreamTower, Tower downstreamTower, double current_)
    {
        win2 = animationWindow;
        t1 = upstreamTower;
        t2 = downstreamTower;
        current = current_;
    }

    public void display()
    {
        win2.fill(255);
        win2.pushMatrix();
        // Find the tower that is farther left or back
        Tower farTower = t1;
        Tower nearTower = t2;
        if (farTower.getZ() > nearTower.getZ() || farTower.getX() > nearTower.getX())
        {
            farTower = t2;
            nearTower = t1;
        }

        //translate to that tower's (x, z)
        win2.rotateX(-win2.PI / 6);
        win2.rotateY(win2.PI / 6);
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
        win2.popMatrix();
    }
}
