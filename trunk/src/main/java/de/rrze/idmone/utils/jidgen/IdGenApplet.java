package de.rrze.idmone.utils.jidgen;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.UIManager;

public class IdGenApplet extends Applet implements ActionListener {

    private static final long serialVersionUID = -3061988014342527435L;

    private static final String DEFAULT_CLI = "-Tf John -Tl Doe -T 2f:[f3,]:2l:[l3,]:N++";

    private IdGenerator idGen;
    private String currentCli = DEFAULT_CLI;

    private Button b_nextID, b_reset;
    private TextField t_cli;
    private TextField t_output;

    private Frame f_mainFrame;
    private MenuBar mb_bar;
    private Menu m_file, m_generator;
    private MenuItem mi_reset, mi_exit, mi_generate;

    private Panel controlPanel, outputPanel, inputPanel;

    public void init() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	/*
	 * Main frame
	 */
	f_mainFrame = new Frame("jidgen GUI");
	f_mainFrame.setSize(550, 300);
	f_mainFrame.setLayout(new BorderLayout());

	f_mainFrame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent we) {
		destroy();
	    }
	});

	/*
	 * Menu bar
	 */
	// File menu
	m_file = new Menu("File");

	mi_exit = new MenuItem("Exit", new MenuShortcut('X'));
	mi_exit.addActionListener(this);
	
	m_file.add(mi_exit);
	
	// Generator menu
	m_generator = new Menu("Generator");

	mi_generate = new MenuItem("Generate ID", new MenuShortcut('G'));
	mi_generate.addActionListener(this);
	
	mi_reset = new MenuItem("Reset", new MenuShortcut('R'));
	mi_reset.addActionListener(this);

	m_generator.add(mi_generate);
	m_generator.add(mi_reset);

	
	
	mb_bar = new MenuBar();
	mb_bar.add(m_file);
	mb_bar.add(m_generator);
	
	f_mainFrame.setMenuBar(mb_bar);


	/*
	 * Panels
	 */
	controlPanel = new Panel();
	outputPanel = new Panel();
	inputPanel = new Panel();

	t_cli = new TextField(currentCli);
	inputPanel.add(t_cli);

	t_output = new TextField(10);
	t_output.setEditable(false);
	t_output.setBackground(Color.WHITE);
	outputPanel.add(t_output);

	b_nextID = new Button("Next ID");
	b_nextID.addActionListener(this);
	controlPanel.add(b_nextID);

	b_reset = new Button("Reset");
	b_reset.addActionListener(this);
	controlPanel.add(b_reset);

	f_mainFrame.add(inputPanel, "North");
	f_mainFrame.add(outputPanel, "Center");
	f_mainFrame.add(controlPanel, "South");

	
	idGen = new IdGenerator(t_cli.getText());
    }

    public void start() {
	f_mainFrame.setVisible(true);
    }

    public void stop() {
	f_mainFrame.setVisible(false);
    }

    public void destroy() {
	System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
	System.out.println("actionPerformed: " + e.getActionCommand());
	
	if (e.getSource() == b_nextID || e.getSource() == mi_generate) {
	    if (!t_cli.getText().equals(currentCli)) {
		idGen = new IdGenerator(t_cli.getText());
		currentCli = t_cli.getText();
	    }

	    List<String> idList = idGen.getNextIDs(1);
	    if (idList.isEmpty()) {
		t_output.setText("No alternatives left.");
		System.out.println("No alternatives left.");
	    } else {
		t_output.setText(idList.get(0));
		System.out.println(idList.get(0));
	    }
	}

	if (e.getSource() == b_reset || e.getSource() == mi_reset) {
	    currentCli = DEFAULT_CLI;
	    t_cli.setText(currentCli);
	    idGen = new IdGenerator(t_cli.getText());

	    t_output.setText("");
	}
	
	if (e.getSource() == mi_exit) {
	    destroy();
	}
    }
}
