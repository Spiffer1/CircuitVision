import java.awt.Frame;

import processing.core.PApplet;

/**
 * Proof of Concept to make two Processing windows appear
 */
public class TwoWindow extends PApplet {

	// PFrame f;
	secondApplet s;

	public static void main(String args[]) 
    {
        PApplet.main(new String[] { "TwoWindow" });
    }
    
	public void setup() {
		size(320, 240);
		frameRate(15);
		new PFrame(); // was PFrame f = new PFrame();
	}

	public void draw() {
		background(255, 0, 0);
		fill(0);
		rect(20, 20, 50, 50 + frameCount);

	}

	public class PFrame extends Frame {
		public PFrame() {
			setBounds(300, 300, 400, 300);
			s = new secondApplet();
			add(s);
			s.init();
			setVisible(true); // was show();
		}
	}

	public class secondApplet extends PApplet {
		public void setup() {
			size(400, 300);
			frameRate(15);
		}

		public void draw() {
			background(0);
			fill(255);
			rect(20, 20, 50, 50 - frameCount);
			redraw();
		}
	}
}

