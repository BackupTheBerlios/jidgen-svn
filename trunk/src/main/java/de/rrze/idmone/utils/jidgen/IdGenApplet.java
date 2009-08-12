package de.rrze.idmone.utils.jidgen;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.TextField;
import java.util.List;

public class IdGenApplet extends Applet {

	private static final long serialVersionUID = -3061988014342527435L;
	
	private static final String DEFAULT_CLI = "-Tf John -Tl Doe -T 2f:[f3,]:2l:[l3,]:N++";
	
	private String output = "";

	private IdGenerator idGen;
	private String currentCli = DEFAULT_CLI;

	
	private Button b_nextID, b_reset;
	private TextField t_cli;
	
	
	
	public void init() {
		t_cli = new TextField(currentCli);
		this.add(t_cli);

		b_nextID = new Button("Next ID");
		this.add(b_nextID);
		
		b_reset = new Button("Reset");
		this.add(b_reset);
	}

	public void start() {
		idGen = new IdGenerator(t_cli.getText());
	}

	public void stop() {
	}

	public void destroy() {
	}

	public boolean action(Event e, Object args) {
		if (e.target == b_nextID) {
			if (!t_cli.getText().equals(currentCli)) {
					idGen = new IdGenerator(t_cli.getText());
					currentCli = t_cli.getText();
			}
			
			List<String> idList = idGen.getNextIDs(1);
			if (idList.isEmpty()) {
				print("No alternatives left.");
				
			}
			else {
				print(idList.get(0));
			}
		}
		
		if (e.target == b_reset) {
			currentCli = DEFAULT_CLI;
			t_cli.setText(currentCli);
			idGen = new IdGenerator(t_cli.getText());			
		}
		
		repaint();
		return true;
	}

	void print(String s) {
		output = s;
		repaint();
	}

	public void paint(Graphics g) {
		// Draw a Rectangle around the applet's display area.
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		// Draw the current string inside the rectangle.
		g.drawString(output, 5, 100);
		output = "";
	}
}
