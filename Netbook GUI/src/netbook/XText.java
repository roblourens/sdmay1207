package netbook;
// Packages
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;



/**
 * Main class that creates the XText window
 * 
 * @author Benjamin Wiemers
 * 
 */
public class XText {

	// Main Function
	public static void main(String[] args) {
		XTextFrame xtext = new XTextFrame();
		xtext.setVisible(true);
		xtext.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}










/**
 * This class creates the frame in the window and holds the text area and all
 * the functionality of the XText text editor
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XTextFrame extends JFrame {

	// Variables
	int DEFAULT_WIDTH = 800;
	int DEFAULT_HEIGHT = 510;
	XMenu menu;
	JTabbedPane tab;

	// Frame constructor
	public XTextFrame() {
		// Set size and title
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setTitle("XText");

		// Set up tab pane
		tab = new JTabbedPane();
		tab.setVisible(true);
		tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// Set up menu
		menu = new XMenu(this);
		menu.setMnemonic(KeyEvent.VK_CONTROL);

		// Add Components to the frame
		this.setJMenuBar((JMenuBar) new XMenuBar(this));

		// Set up tree
		XTree tree = new XTree(menu);

		// Set up Split pane
		JSplitPane sPane = new JSplitPane();
		sPane.setDividerSize(5);
		sPane.setContinuousLayout(true);
		sPane.setRightComponent(tab);
		sPane.setLeftComponent(new JScrollPane(tree));
		getContentPane().add(sPane);

	}

	// Function creates a new tab with the title "NEW"
	public void knew() {
		// Create new tab
		System.out.println("Adding new tab");
		final XTextPane pane = new XTextPane(this);
		tab.addTab("New", pane);
		
		// Set up the tab top
		updateTab("New", tab.indexOfComponent(pane));
	}

	// Create new tab based off the file give
	public void knew(final String dir) {
		// Set up the tab
		String title = dir.substring(dir.lastIndexOf("/") + 1);
		System.out.println("Adding Tab " + title);	
		final XTextPane pane = new XTextPane(this);
		pane.setVisible(true);
		tab.addTab(dir, pane);
		updateTab(dir, tab.indexOfComponent(pane));
	}
	
	// This function updates the top of the tab with the given file information 

	public void updateTab(final String file, int index) {
		// Get title
		int slash = file.lastIndexOf("/");
		String title = "New";
		if(slash >= 0){
			title = file.substring(slash + 1);
		}
		
		// Make the button to close out the tab
		JButton exit = new JButton("X");
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				menu.exit(file);
			}

		});
		// Make tab top components and add them
		JLabel name = new JLabel("XText:" + title);
		JPanel top = new JPanel();
		name.setFont(new Font("Serif", Font.PLAIN, 10));
		exit.setFont(new Font("Serif", Font.PLAIN, 10));
		top.add(exit);
		top.add(name);

		// Set the tab top
		tab.setTabComponentAt(index, top);
	}
} // END OF XTEXTFRAME









/**
 * This is the defined popup menu for the XText This is created by the
 * XTextFrame class This class calls methods from the XMenu class
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XPopupMenu extends JPopupMenu implements ActionListener {

	// Variables
	JMenuItem replace = new JMenuItem("Replace");
	JMenuItem replace_all = new JMenuItem("Replace All");
	JMenuItem find = new JMenuItem("Find");

	XMenu menu;
	JTabbedPane tab;

	// Constructor to create the pop up menu
	// Allows the user three functionalities
	public XPopupMenu(XTextFrame frame) {
		// Add the menu items
		this.menu = frame.menu;
		this.tab = frame.tab;
		add(replace);
		add(replace_all);
		add(find);

		// Add action listener
		replace.addActionListener(this);
		replace_all.addActionListener(this);
		find.addActionListener(this);

	}

	// The action listener for the pop up menu
	@Override
	public void actionPerformed(ActionEvent event) {
		JTextArea text = ((XTextPane) tab.getComponentAt(tab.getSelectedIndex())).text;

		// Output to command prompt the Event in acted
		String cmd = ((JMenuItem) event.getSource()).getText();
		System.out.println("Event: " + cmd);

		// The replace function
		if (cmd.equals(replace.getText())) {
			System.out
					.println("Entering  REPLACE functionality from popup menu");
			menu.replace(text); // Enter the XMenu replace function
			System.out
					.println("Exiting  REPLACE functionality from popup menu\n");

			// The replace_all function
		} else if (cmd.equals(replace_all.getText())) {
			System.out
					.println("Entering  REPLACE ALL functionality from popup menu");
			menu.replace_all(text); // Enter the XMenu replace function
			System.out
					.println("Exiting  REPLACE ALL functionality from popup menu\n");

			// The find function
		} else if (cmd.equals(find.getText())) {
			System.out.println("Entering  FIND functionality from popup menu");
			menu.find(text); // Enter the XMenu find function
			System.out.println("Exiting  FIND functionality from popup menu\n");

		}

	}
}










/**
 * This is the top menu bar This calls functions from XMenu
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XMenuBar extends JMenuBar implements ActionListener {

	// Variables
	JMenuItem knew = new JMenuItem("New...");
	JMenuItem open = new JMenuItem("Open");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem exit = new JMenuItem("Exit");
	JMenuItem replace = new JMenuItem("Replace");
	JMenuItem replace_all = new JMenuItem("Replace All");
	JMenuItem find = new JMenuItem("Find");
	JMenuItem small = new JMenuItem("Small");
	JMenuItem medium = new JMenuItem("Medium");
	JMenuItem large = new JMenuItem("Large");

	XMenu menu;
	JTabbedPane tab;

	// Constructor for the menu bar
	public XMenuBar(XTextFrame frame) {
		this.menu = frame.menu;
		this.tab = frame.tab;
		// Create Components
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu font = new JMenu("Font Size Options");

		// Add action listeners
		knew.addActionListener(this);
		open.addActionListener(this);
		save.addActionListener(this);
		exit.addActionListener(this);
		replace.addActionListener(this);
		replace_all.addActionListener(this);
		find.addActionListener(this);
		small.addActionListener(this);
		medium.addActionListener(this);
		large.addActionListener(this);

		// Set Mnemonics
		file.setMnemonic(KeyEvent.VK_F);
		edit.setMnemonic(KeyEvent.VK_E);
		font.setMnemonic(KeyEvent.VK_S);
		knew.setMnemonic(KeyEvent.VK_N);
		open.setMnemonic(KeyEvent.VK_O);
		save.setMnemonic(KeyEvent.VK_S);
		exit.setMnemonic(KeyEvent.VK_X);
		replace.setMnemonic(KeyEvent.VK_R);
		replace_all.setMnemonic(KeyEvent.VK_A);
		find.setMnemonic(KeyEvent.VK_F);
		small.setMnemonic(KeyEvent.VK_S);
		medium.setMnemonic(KeyEvent.VK_M);
		large.setMnemonic(KeyEvent.VK_L);

		// Add visual
		add(file);
		add(edit);

		file.add(knew);
		file.add(open);
		file.add(save);
		file.add(exit);

		edit.add(replace);
		edit.add(replace_all);
		edit.add(find);
		edit.add(font);

		font.add(small);
		font.add(medium);
		font.add(large);

	}

	// This is the action performed for the menu bar
	@Override
	public void actionPerformed(ActionEvent event) {

		// Output the event to the command prompt
		String cmd = ((JMenuItem) event.getSource()).getText();
		System.out.println("Event: " + cmd);

		// New Function
		if (cmd.equals(knew.getText())) {
			System.out.println("Entering NEW functionality");
			menu.knew(); // Call to XMenu
			System.out.println("Exiting NEW functionality\n");

			// Open Function
		} else if (cmd.equals(open.getText())) {
			System.out.println("Entering  OPEN functionality");
			menu.open(); // Call to XMenu
			// if user did not cancel then change title
			System.out.println("Exiting  OPEN functionality\n");

			// Save Function
		} else if (cmd.equals(save.getText())) {
			System.out.println("Entering  SAVE functionality");
			menu.save(); // Call to XMenu
			// if user did not cancel then change title
			System.out.println("Exiting  SAVE functionality\n");

			// Exit Function
		} else if (cmd.equals(exit.getText())) {
			System.out.println("Entering  EXIT functionality");
			menu.exit(); // Call to XMenu
			System.out.println("Exiting  EXIT functionality\n");

			// Replace Function
		} else if (cmd.equals(replace.getText())) {
			System.out.println("Entering  REPLACE functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.replace(text); // Call to XMenu
			}
			System.out.println("Exiting  REPLACE functionality\n");

			// Replace_all function
		} else if (cmd.equals(replace_all.getText())) {
			System.out.println("Entering  REPLACE ALL functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.replace_all(text); // Call to XMenu
			}
			System.out.println("Exiting  REPLACE ALL functionality\n");

			// Find Function
		} else if (cmd.equals(find.getText())) {
			System.out.println("Entering  FIND functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.find(text); // Call to XMenu
			}
			System.out.println("Exiting FIND functionality\n");

			// set font to small function
		} else if (cmd.equals(small.getText())) {
			System.out.println("Entering  SMALL functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.setFontSize(10, text); // Call to XTextFrame
			}
			System.out.println("Exiting  SMALL functionality\n");

			// set font to medium function
		} else if (cmd.equals(medium.getText())) {
			System.out.println("Entering  MEDIUM functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.setFontSize(15, text); // Call to XTextFrame
			}
			System.out.println("Exiting  MEDIUM functionality\n");

			// set font to large function
		} else if (cmd.equals(large.getText())) {
			System.out.println("Entering  LARGE functionality");
			int index = tab.getSelectedIndex();
			if (index < 0) {
				System.out.println("No selected Tab");
				menu.sendError("No selected tab");
			} else {
				JTextArea text = ((XTextPane) tab.getComponentAt(index)).text;
				menu.setFontSize(20, text); // call to XTextFrame
			}
			System.out.println("Exiting  LARGE functionality\n");
		}
	}
}










/**
 * 
 * This is the XMenu that will do the actual functionality work
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XMenu extends JMenu {

	// Variables
	JFileChooser fchoose;
	FileNameExtensionFilter ffilter;


	XTextFrame frame;
	JTabbedPane tab;

	// Constructor
	public XMenu(XTextFrame frame) {

		this.tab = frame.tab;
		this.frame = frame;

		// Set up the file chooser
		fchoose = new JFileChooser();
		ffilter = new FileNameExtensionFilter("TEX & TEXT & TXT Files", "tex", "text", "txt");
		fchoose.setFileFilter(ffilter);

	}


	// Sends an error message
	public void sendError(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	// Searches the text for a value
	public int search(String input, JTextArea text) {
		// Get position of caret
		int pos = text.getCaretPosition();
		boolean firsthalf = false;
		System.out.println("Caret Position is: " + pos);

		// Get text from text area
		String txt = text.getText();

		// Search for input
		int loc = txt.substring(pos).indexOf(input);
		System.out.println("Searching \"" + txt.substring(pos) + "\"\tloc:"
				+ loc);
		if (loc < 0) {
			firsthalf = true;
			loc = txt.indexOf(input);
			System.out.println("Searching \"" + txt.substring(0, pos)
					+ "\"\tloc:" + loc);

			// If not found
			if (loc < 0) {
				text.select(0, 0);
				return -1;
			}
		}
		System.out.println("Found Occurance at: " + loc);

		// Set caret position to front of word and highlight
		if (firsthalf) {
			text.setCaretPosition(loc);
			text.select(loc, loc + input.length());
			return loc;

		} else {
			text.setCaretPosition(pos + loc);
			text.select(loc + pos, loc + pos + input.length());
			return loc + pos;

		}
	}

	// Open a file and output the text to the user for editing
	/* Obtained from homework SE/ComS 319 ... */
	public void openFile(String dir, JTextArea text) {

		try {
			// Clear text area and create the file reader
			text.setText("");
			BufferedReader reader = new BufferedReader(new FileReader(dir));
			String line = null;

			// While the file still has data, output to the text area
			while ((line = reader.readLine()) != null) {
				text.append(line);
			}

			// Close the file
			reader.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// Save the file to the file specified by the user
	/* Obtained from homework SE/ComS 319 ... */
	public void saveFile(String file, JTextArea text) {

		try {
			// Open file and write to file
			PrintWriter out = new PrintWriter(new FileWriter(file));
			out.print(text.getText());

			// If any errors
			if (out.checkError())
				throw new IOException("Error while writing to file.");

			// close the file and the writer
			out.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// See if any data is selected in the text area
	public boolean hasSelected(JTextArea text) {
		String txt = text.getSelectedText();
		if (txt == null)
			return false;
		return true;
	}

	// Open a new document and if the current document
	// was modified then give the user the option to
	// save the document
	public void knew() {

		// Establish new tab
		frame.knew();
		int index = tab.indexOfTab("New");
		XTextPane pane = (XTextPane) tab.getComponentAt(index);
		pane.origtext = pane.text.getText();
		tab.setSelectedIndex(index);

	}

	// Allows the user to choose a file to open
	// a file from the file system
	public String open() {

		// Allow user to choose file to open
		int retVal = fchoose.showOpenDialog(this);

		// If user did not cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			// open the file and set the title and output the data.
			String file = fchoose.getSelectedFile().getPath();
			System.out.print("File choosen: " + file);
		
			// Check to see if already opened
			int index = tab.indexOfTab(file);
			System.out.print("Searching for tab ...");
			if (index >= 0) {
				System.out.println("Found tab");
				tab.setSelectedIndex(index);
				return file;
			}
			System.out.println("Tab not found, making new one");
			frame.knew(file);
			index = tab.indexOfTab(file);
			XTextPane pane = (XTextPane) tab.getComponentAt(index);
			openFile(file, pane.text);
			pane.origtext = pane.text.getText();
			tab.setSelectedIndex(index);

			// return the file to the calling class to let them know it worked
			return file;
		}
		// if the user canceled, let the calling class.
		return null;
	}

	// Allows the user to choose a file to open
	// from the tree. 
	public void openFromTree(String dir) {
		System.out.print("OpenFromTree: dir -> " + dir);

		// Get Title
		String title = dir.substring(dir.lastIndexOf("/") + 1);
		System.out.println("\tTitle: " + title);

		// Check the title
		int t0 = title.indexOf(".tex");
		int t1 = title.indexOf(".text");
		int t2 = title.indexOf(".txt");

		if (t0 < 0 && t1 < 0 && t2 < 0) {
			sendError("Cannot open this file: " + dir);
			return;
		}

		// Check to see if already opened
		int index = tab.indexOfTab(dir);
		System.out.print("Looking for tab....");
		if (index >= 0) {
			tab.setSelectedIndex(index);
			System.out.println("Tab found");
			return;
		}
		System.out.println("Tab not found");
		frame.knew(dir);
		index = tab.indexOfTab(dir);
		System.out.println("Index of tab "+index);
		XTextPane pane = (XTextPane) tab.getComponentAt(index);
		openFile(dir, pane.text);
		pane.origtext = pane.text.getText();
		System.out.println("Origtext: "+pane.origtext);
		tab.setSelectedIndex(index);
	}

	
	// Gives the user the option to save select a file to save to
	public String save() {
		// Allow user to choose file to open
		int index = tab.getSelectedIndex();
		XTextPane pane = (XTextPane) tab.getComponentAt(index);
		JTextArea text = pane.text;
		// Allow user to select file
		int retVal = fchoose.showSaveDialog(this);

		// if user did not cancel, save file and update information
		if (retVal == JFileChooser.APPROVE_OPTION) {
			String file = fchoose.getSelectedFile().getPath();
			System.out.println("File choosen: " + file);
			saveFile(file, text);
			frame.updateTab(file,index);
			pane.origtext = text.getText();
			return file;
		}
		// if user canceled
		return null;
	}

	// This function allows a tab to be saved by
	// being given the index of the tab
	public String save(int index) {
		// Get the tab components
		XTextPane pane = (XTextPane) tab.getComponentAt(index);
		JTextArea text = pane.text;

		// Allow user to select file
		int retVal = fchoose.showSaveDialog(this);

		// if user did not cancel, save file and update information
		if (retVal == JFileChooser.APPROVE_OPTION) {
			String file = fchoose.getSelectedFile().getPath();
			System.out.println("File choosen: " + file);
			saveFile(file, text);

			pane.origtext = text.getText();
			
			return file;
		}
		// if user canceled
		return null;
	}
	
	// Allows the user to exit XText and 
	// if any of the tab contained modified 
	// data, the user is allowed to save the 
	// data.

	// Exits the text frame, if data was modified, allows user to save file
	public void exit() {
		// Get total number of tabs
		int count = tab.getTabCount();
		System.out.println("There are " + count + " tabs open.");
		
		// Iterate through all the labs
		for (int i = 0; i < count; i++) {
			// Get the tab components and let the user know about which tab
			tab.setSelectedIndex(count-i-1);
			XTextPane pane = (XTextPane) tab.getComponentAt(count-i-1);
			System.out.print(i);
			
			// If text was modified, then save
			System.out.println(" Text: \"" + pane.text.getText()
					+ "\"\tOriginal: \"" + pane.origtext + "\"");
			if (!pane.text.getText().equals(pane.origtext)) {
				System.out.println("Saving File");
				save(count-i-1);
			}
			tab.remove(count-i-1);
		}

		// Goodbye
		System.out.println("\nGood Bye");
		System.exit(0);
	}

	
	// This exits out of a particular tab 
	// that is given by the title supplied
	public void exit(String title){
		// Get the tab components
		int index = tab.indexOfTab(title);
		XTextPane pane = (XTextPane) tab.getComponentAt(index);
			
		// If contains modified data
		if (!pane.text.getText().equals(pane.origtext)) {
			save(index);
		}
		// remove tab
		tab.remove(index);	
	}
	
	// This will replace data with other data
	
	// Replaces a value with another value
	// Can be called by either the pop up menu or the menu bar
	// Looks for any data highlighted and replaces that with a
	// value entered by the user. If no data is highlighted
	// then allow user to enter data to be replaced.
	public void replace(JTextArea text) {

		// string to be replaced
		String input = null;

		// check for highlighted data
		boolean selected = hasSelected(text);

		// If has highlighted data, use the highlighted data
		if (selected) {
			input = text.getSelectedText();
			// If no highlighted data, prompt user
		} else {
			input = JOptionPane.showInputDialog("Replace: ");
		}

		// If user did not cancel
		if (input != null) {
			// ask user for what to replace it with
			System.out.println("Replacing: \"" + input + "\"");
			String replacement = JOptionPane.showInputDialog("Replace with: ");
			System.out.println("Replacing with: " + replacement);

			// Search for the string to be replaced
			int find = search(input, text);

			// if user did not cancel and the value to be replaced was found
			if (replacement != null && find >= 0) {
				// replace the value
				int start = text.getSelectionStart();
				int end = text.getSelectionEnd();
				String txt = text.getText();
				String newText = txt.substring(0, start) + replacement
						+ txt.substring(end, txt.length());
				text.setText(newText);

				// if not found
			} else if (replacement != null && find < 0) {
				System.out.println("\"" + input + "\"  Not Found");
				sendError("\"" + input + "\"  Not Found");
			}
			// if user canceled the function
		} else {
			System.out.println("Cancelled");
		}
	}

	// Same behavior as replace but replaces ALL targeted words in the text
	
	// This will replace all data of one sort with other data
	public void replace_all(JTextArea text) {
		// Variables
		int find = 0;
		String input = null;

		// has selected
		boolean selected = hasSelected(text);

		// if there is selected
		if (selected) {
			input = text.getSelectedText();
			// else get user input
		} else {
			input = JOptionPane.showInputDialog("Replace: ");
		}

		// if user did not cancel
		if (input != null) {

			// Get value to replace with
			System.out.println("Replacing: \"" + input + "\"\t");
			String replacement = JOptionPane.showInputDialog("Replace with: ");
			System.out.println("Replacing with: " + replacement);
						
			// Find value
			find = search(input, text);
			// If user didn't cancel and replace value is found in text
		
			if(replacement != null && replacement.equals(input)){
				sendError("Cannot replace a string with itself");
			} else if (replacement != null && find >= 0) {
				// While some are still left
				while (find >= 0) {
					int start = text.getSelectionStart();
					int end = text.getSelectionEnd();
					String txt = text.getText();
					String newText = txt.substring(0, start) + replacement
							+ txt.substring(end, txt.length());
					text.setText(newText);
					find = search(input, text);
				}
				// If user didn't cancel and not initially found
			} else if (replacement != null && find < 0) {
				System.out.println("\"" + input + "\" Not Found");
			} 
		} else {
			System.out.println("Cancelled");
		}
	}

	// Prompts the user for a value to find in the text then highlight the text
	
	// This will locate any data
	public void find(JTextArea text) {
		// Prompt user
		String input = JOptionPane.showInputDialog("Find: ");
		if (input != null) {
			System.out.println("Finding: \"" + input + "\"");
			// Search the text for the value
			if (search(input, text) < 0)
				JOptionPane.showMessageDialog(this, "Could not find " + input); // Calls
																				// XMenu
																				// search

			// If user canceled
		} else {
			System.out.println("Cancelled");
		}
	}

	// sets the font
	
	// This will set the font size of the text area
	public void setFontSize(int size, JTextArea text) {
		text.setFont(new Font("Serif", Font.PLAIN, size)); // Calls XTextFrame
															// to set font
	}

}










/**
 * This class is the main text 
 * area where the data goes
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XTextPane extends JScrollPane {
	// Variables
	JTextArea text;
	String origtext;

	// Constructor
	public XTextPane(final XTextFrame frame) {
		// Set up the text area
		text = new JTextArea();
		text.setFont(new Font("Serif", Font.PLAIN, 10));
		text.setLineWrap(true);
		text.setVisible(true);
		add(text);
		this.setViewportView(text);
		
		// Add mouse listener and define the action of the mouse listener
		text.addMouseListener(new MouseListener() {
			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
			}

			// If mouse is pressed
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					popup(e);
			}

			// or if mouse is released
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					popup(e);
			}

			// If right button is clicked then create
			// the defined popup menu and show the
			// menu
			public void popup(MouseEvent event) {
				int button = event.getButton();
				if (button == 3) {
					System.out.println("Opened Popup Menu");
					XPopupMenu pop = new XPopupMenu(frame);
					pop.show(event.getComponent(), event.getX(), event.getY());
				}

			}

		});

	}
}







/**
 * This class establishes the tree
 * in the right hand panel of the split panel.
 * 
 * @author Benjamin Wiemers
 *
 */
@SuppressWarnings("serial")
class XTree extends JTree {
	// Variables
	DefaultTreeModel model;
	DefaultMutableTreeNode root;
	XMenu menu;

	// Constructor
	XTree(final XMenu menu) {
		// Set up  
		this.menu = menu;
		String curdir = System.getProperty("user.home");
		root = new DefaultMutableTreeNode(curdir);
		if ((new File(curdir).isDirectory()))
			root.add(new DefaultMutableTreeNode(""));

		// Create the tree model
		model = new DefaultTreeModel(root);
		setModel(model);
		setEditable(false);
		setRootVisible(true);
		setShowsRootHandles(true);
		collapseRow(0);

		// Add the listener who listens for when an element is selected
		addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent event) {
				// Get the event node and the path
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event
						.getPath().getLastPathComponent();
				TreeNode[] path = (TreeNode[]) node.getPath();
				String curdir = "";

				// If it is the root node
				if (!((String) ((DefaultMutableTreeNode) path[0])
						.getUserObject()).equals(root.getUserObject())) {
					return;
				
				}
				
				// Build the path of the file that the node represents
				for (int i = 0; i < path.length; i++)
					curdir += ((DefaultMutableTreeNode) path[i])
							.getUserObject() + "/";

				/// Test if the file is a directory
				File dir = new File(curdir);
				if (!dir.isDirectory())
					menu.openFromTree((String) curdir.substring(0,
							curdir.length() - 1));
				else
					System.out.println(curdir + " is a directory\n");
			}
		});

		// Add a listener who listens for when the tree is expanded
		addTreeWillExpandListener(new TreeWillExpandListener() {

			// If the tree collapse then remove the children
			@Override
			public void treeWillCollapse(TreeExpansionEvent event)
					throws ExpandVetoException {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				System.out.println("In TreeWillCollapseListener: Removing Children of "
								+ node.getUserObject());
				unexploreTree(node);
			}
			
			// If the tree will expand then add the children
			@Override
			public void treeWillExpand(TreeExpansionEvent event)
					throws ExpandVetoException {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
				System.out.println("In TreeWillExpandListener: Getting next layer...");
				exploreTree(node);
				System.out.println("Done\n");
			}

		});

	}

	// This function explores a section of the tree and adds any children
	private boolean exploreTree(DefaultMutableTreeNode dirNode) {
		// get the path of the nodes
		TreeNode[] path = (TreeNode[]) dirNode.getPath();
		String curdir = "";
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dirNode.getParent();

		// If it is a root node
		if (!((String) ((DefaultMutableTreeNode) path[0]).getUserObject()).equals(root.getUserObject())) {
			return false;
		}
		// Build the path
		for (int i = 0; i < path.length; i++)
			curdir += ((DefaultMutableTreeNode) path[i]).getUserObject() + "/";
		File dir = new File(curdir);
		
		// Explore how many children there are of the node
		System.out.println("Exploring " + curdir);
		int count = dirNode.getChildCount();
		DefaultMutableTreeNode child;
		for (int i = 0; i < count; i++) {
			child = (DefaultMutableTreeNode) dirNode.getFirstChild();
			model.removeNodeFromParent(child);
		}
		
		// Get the children
		String[] children = dir.list();
		
		// If there are children
		if (children != null)
			// iterate through each one
			for (int i = 0; i < children.length; i++) {
				// create the child node
				System.out.print("Child " + children[i] + ": ");
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(children[i]);
				
				// And add to the tree
				int index = 0;
				if (parent != null)parent.getIndex(dirNode);
				model.insertNodeInto(node, dirNode, index);
				if ((new File(dir + "/" + children[i])).isDirectory()) {
					System.out.print("Adding fake node and ");
					model.insertNodeInto(new DefaultMutableTreeNode(""), node,
							node.getParent().getIndex(node));
				}
				
				System.out.println("Adding child to the" + curdir + " node");

			}
		else {
			// If there are no children then add a fake one to keep the folder image in the tree
			model.insertNodeInto(new DefaultMutableTreeNode(null), dirNode, dirNode.getParent().getIndex(dirNode));
			System.out.println("no children");
		}
	
		return true;
	}
	
	// this function unexplores a section of a tree by removing any children of a node

	private boolean unexploreTree(DefaultMutableTreeNode node) {
		// get child count
		System.out.print("Unexploring " + node.getUserObject());
		int count = node.getChildCount();
		System.out.println(": Count " + count);
		DefaultMutableTreeNode child;
		
		// iterate through the chilern and remove them
		for (int i = 0; i < count; i++) {
			child = (DefaultMutableTreeNode) node.getFirstChild();
			model.removeNodeFromParent(child);
		}
		
		// Add a fake node to keep the folder image
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		int index = 0;
		if (parent != null)	parent.getIndex(node);
		model.insertNodeInto(new DefaultMutableTreeNode(""), node,index);
		
		return true;
	}
}
