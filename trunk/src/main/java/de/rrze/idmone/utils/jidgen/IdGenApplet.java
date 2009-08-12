package de.rrze.idmone.utils.jidgen;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class IdGenApplet extends Applet implements MouseListener {

	private static final long serialVersionUID = -3061988014342527435L;
	private StringBuffer buffer;

	private IdGenerator idGen;
	
	
	public void init() {
		addMouseListener(this);
		buffer = new StringBuffer();
		addItem("initializing... ");
	
	
		idGen = new IdGenerator("-Tf John -Tl Doe -T 2f:2l:N4++");
	
	}

	public void start() {
		addItem("starting... ");
	}

	public void stop() {
		addItem("stopping... ");
	}

	public void destroy() {
		addItem("preparing for unloading...");
	}

	void addItem(String newWord) {
		System.out.println(newWord);
		buffer.append(newWord);
		repaint();
	}

	public void paint(Graphics g) {
		// Draw a Rectangle around the applet's display area.
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		// Draw the current string inside the rectangle.
		g.drawString(buffer.toString(), 5, 15);
	}

	// The following empty methods could be removed
	// by implementing a MouseAdapter (usually done
	// using an inner class).
	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
	}

	public void mousePressed(MouseEvent event) {
	}

	public void mouseReleased(MouseEvent event) {
	}

	public void mouseClicked(MouseEvent event) {
		//addItem("click!... ");
		List<String> idList = idGen.generateIDs(1);
		addItem("ID: " + idList.get(0));
	}
}
