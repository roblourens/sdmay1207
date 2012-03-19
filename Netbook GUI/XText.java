package homework2;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 * Main class that creates the XText window
 * 
 * @author Benjamin Wiemers
 *
 */
public class XText{
	
	// Main Function
	public static void main(String[] args) {
		XTextFrame xtext = new XTextFrame();
		xtext.setVisible(true);
        xtext.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
	}
}




/**
 * This class creates the frame in the window and
 * holds the text area and all the functionality of 
 * the XText text editor
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XTextFrame extends JFrame {
		
	// Variables
	int DEFAULT_WIDTH = 500;
	int DEFAULT_HEIGHT = 510;  
	JTextArea text;
	
	public XTextFrame() {
		// Set size and title
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setTitle("XText: New");
        
        // Create TextArea, and Menubars
        text = new JTextArea();
        text.setFont(new Font("Serif",Font.PLAIN,10));
        text.setLineWrap(true);
        
        final XMenu menu = new XMenu(this);
        menu.setMnemonic(KeyEvent.VK_CONTROL);
        
        // Add mouse listener and define the action of the mouse listener
        text.addMouseListener(new MouseListener(){
	    	@Override
	    	public void mouseEntered(MouseEvent e) {}
	    	@Override
	    	public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {}
	
	    	// If mouse is pressed
	    	@Override
	    	public void mousePressed(MouseEvent e) {if(e.isPopupTrigger()) popup(e);}
	    	// or if mouse is released
	    	@Override
	    	public void mouseReleased(MouseEvent e){if(e.isPopupTrigger()) popup(e);}
	    	
	    	// If right button is clicked then create
	    	// the defined popup menu and show the 
	    	// menu
	    	public void popup(MouseEvent event){
	    		int button = event.getButton(); 
	    		if(button == 3){
	    			System.out.println("Opened Popup Menu");
	    			XPopupMenu pop = new XPopupMenu(menu);
	    			pop.show(event.getComponent(), event.getX(), event.getY());
	    		}
	    		
	    	}

    	});
    	        
        // Add Components to the frame
        this.setJMenuBar((JMenuBar) new XMenuBar(menu));
        getContentPane().add(new JScrollPane(text));
        
        
	}
	
	/**
	 * This sets the title of the window
	 * 
	 * @param title the new title of the window
	 */
	public void changeTitle(String title){
		setTitle("XText: "+title);
	}
}

/**
 * This is the defined popup menu for the XText
 * This is created by the XTextFrame class
 * This class calls methods from the XMenu class
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XPopupMenu extends JPopupMenu implements ActionListener{
	
	//Variables
	JMenuItem replace = new JMenuItem("Replace");
	JMenuItem replace_all = new JMenuItem("Replace All");
	JMenuItem find = new JMenuItem("Find");
	
	XMenu menu;
	
	// Constructor to create the pop up menu
	// Allows the user three functionalities
	public XPopupMenu(XMenu menu){
		// Add the menu items
		this.menu = menu;
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
		// Output to command prompt the Event in acted 
		String cmd = ((JMenuItem) event.getSource()).getText();
		System.out.println("Event: "+cmd);
		
		// The replace function
		if(cmd.equals(replace.getText())){
			System.out.println("Entering  REPLACE functionality from popup menu");
			menu.replace();	// Enter the XMenu replace function
			System.out.println("Exiting  REPLACE functionality from popup menu\n");
				
		// The replace_all function
		} else if(cmd.equals(replace_all.getText())){
			System.out.println("Entering  REPLACE ALL functionality from popup menu");
			menu.replace_all();	// Enter the XMenu replace function
			System.out.println("Exiting  REPLACE ALL functionality from popup menu\n");
			 
		// The find function
		} else if(cmd.equals(find.getText())){
			System.out.println("Entering  FIND functionality from popup menu");
			menu.find();	// Enter the XMenu find function
			System.out.println("Exiting  FIND functionality from popup menu\n");
			
		}
		
	}	
}

/**
 * This is the top menu bar
 * This calls functions from XMenu
 * 
 * @author Benjamin Wiemers
 */
@SuppressWarnings("serial")
class XMenuBar extends JMenuBar implements ActionListener{

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
	
	// Constructor for the menu bar
	public XMenuBar (XMenu menu){
		this.menu = menu;
		
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
		System.out.println("Event: "+cmd);
		
		// New Function
		if(cmd.equals(knew.getText())){
			System.out.println("Entering NEW functionality");
			menu.knew();		// Call to XMeny
			menu.changeTitle("New");	// Change title
			System.out.println("Exiting NEW functionality\n");
			
		// Open Function
		} else if(cmd.equals(open.getText())){
			System.out.println("Entering  OPEN functionality");
			String file = menu.open();	// Call to XMenu
			// if user did not cancel then change title
			if(file!=null) menu.changeTitle(file);
			System.out.println("Exiting  OPEN functionality\n");
			
		// Save Function
		} else if(cmd.equals(save.getText())){
			System.out.println("Entering  SAVE functionality");
			String file = menu.save();		// Call to XMenu
			// if user did not cancel then change title
			if(file!=null) menu.changeTitle(file);	
			System.out.println("Exiting  SAVE functionality\n");
			
		// Exit Function
		} else if(cmd.equals(exit.getText())){
			System.out.println("Entering  EXIT functionality");
			menu.exit();		// Call to XMenu
			System.out.println("Exiting  EXIT functionality\n");
			
		// Replace Function
		} else if(cmd.equals(replace.getText())){
			System.out.println("Entering  REPLACE functionality");
			menu.replace();		// Call to XMenu
			System.out.println("Exiting  REPLACE functionality\n");
				
		//Replace_all function
		} else if(cmd.equals(replace_all.getText())){
			System.out.println("Entering  REPLACE ALL functionality");
			menu.replace_all();	// Call to XMenu
			System.out.println("Exiting  REPLACE ALL functionality\n");
			 
		// Find Function
		} else if(cmd.equals(find.getText())){
			System.out.println("Entering  FIND functionality");
			menu.find();	// Call to XMenu
			System.out.println("Exiting FIND functionality\n");
			
		// set font to small function	
		} else if(cmd.equals(small.getText())){
			System.out.println("Entering  SMALL functionality");
			menu.setFontSize(10);	// Call to XTextFrame
			System.out.println("Exiting  SMALL functionality\n");
			
		// set font to medium function			
		} else if(cmd.equals(medium.getText())){
			System.out.println("Entering  MEDIUM functionality");
			menu.setFontSize(15); // Call to XTextFrame
			System.out.println("Exiting  MEDIUM functionality\n");
			
		// set font to large function
		} else if(cmd.equals(large.getText())){
			System.out.println("Entering  LARGE functionality");
			menu.setFontSize(20);	// call to XTextFrame 
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
class XMenu extends JMenu{
	
	// Variables
	JTextArea text;
	JFileChooser fchoose;
	FileNameExtensionFilter ffilter;
	String fileName;
	String origText = "";

	XTextFrame frame;
	
	// Constructor
	public XMenu(XTextFrame frame){
		// Set the variables
		this.frame = frame;
		this.text = frame.text;
		
		// Set up the file chooser
		fchoose = new JFileChooser();
		ffilter = new FileNameExtensionFilter("TEX & TEXT Files", "tex", "text");
		fchoose.setFileFilter(ffilter);
		fileName = "New";
	}
	
	// Changes the title
	public void changeTitle(String title){
		frame.changeTitle(title);	// call to XTextFrame
	}
	
	// Searches the text for a value
	public int search(String input){
		// Get position of caret
		int pos = text.getCaretPosition();
		boolean firsthalf = false;
		System.out.println("Caret Position is: "+pos);
		
		// Get text from text area
		String txt = text.getText();
		
		// Search for input
		int loc = txt.substring(pos).indexOf(input);
		System.out.println("Searching \""+txt.substring(pos)+"\"\tloc:"+loc);
		if(loc < 0) {	
			firsthalf = true;
			loc = txt.indexOf(input);
			System.out.println("Searching \""+txt.substring(0,pos)+"\"\tloc:"+loc);
			
			// If not found
			if(loc < 0) {
				text.select(0, 0);
				return -1;
			}
		} 
		System.out.println("Found Occurance at: "+loc);
		
		// Set caret position to front of word and highlight
		if(firsthalf){
			text.setCaretPosition(loc);
			text.select(loc, loc+input.length());
			return loc;
			
		}	else {
			text.setCaretPosition(pos+loc);
			text.select(loc+pos, loc+pos+input.length());
			return loc+pos;
			
		}
	}
	
	// Open a file and output the text to the user for editing
	/* Obtained from homework SE/ComS 319 ... */
	public void openFile(String file){
		
		try	{
			// Clear text area and create the file reader
			text.setText("");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			
			// While the file still has data, output to the text area
			while ((line = reader.readLine()) != null)	{
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
	public void saveFile(String file){

		try	{
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
	public boolean hasSelected() {
		String txt = text.getSelectedText();
		if(txt==null) return false;
		return true;
	}
	
	// Open a new document and if the current document
	// was modified then give the user the option to 
	// save the document
	public void knew(){

		// if the data was modified call save
		String txt = text.getText();
		if(!txt.equals(origText)){
			save();
		}
		System.out.println("Entering  NEW... functionality");
		
		// Establish new document
		text.setText("");
		fileName = "New";		
	}	
	
	// Allows the user to choose a file to open
	// and if the data was modified allows the 
	// user to save the data
	public String open(){
		
		// if modified
		String txt = text.getText();
		if(!txt.equals(origText)){
			save();
		}
		
		// Allow user to choose file to open
		int retVal = fchoose.showOpenDialog(this);
		
		// If user did not cancel
		if(retVal == JFileChooser.APPROVE_OPTION){
			// open the file and set the title and output the data.
			String file = fchoose.getSelectedFile().getPath();
			System.out.println("File choosen: "+file);
			openFile(file);
			origText = text.getText();
			fileName = file;
			
			// return the file to the calling class to let them know it worked
			return file;
		}
		// if the user canceled, let the calling class.
		return null;
	}
	
	// Gives the user the option to save select a file to save to
	public String save(){
		
		// Allow user to select file
		int retVal = fchoose.showSaveDialog(this);
		// if user did not cancel, save file and update information
		if(retVal == JFileChooser.APPROVE_OPTION){
			String file = fchoose.getSelectedFile().getPath();
			System.out.println("File choosen: "+file);
			saveFile(file);
			fileName = file;
			origText = text.getText();
			return file;
		}
		// if user canceled
		return null;
	} 
	
	// Exits the text frame, if data was modified, allows user to save file
	public void exit(){
		// If text was modified, then save
		String txt = text.getText();
		if(!txt.equals(origText)){
			save();
		}
		
		// Goodbye
		System.out.println("\nGood Bye");
		System.exit(0);
	}
	
	// Replaces a value with another value
	// Can be called by either the pop up menu or the menu bar
	// Looks for any data highlighted and replaces that with a
	// value entered by the user. If no data is highlighted
	// then allow user to enter data to be replaced.
	public void replace(){
		
		// string to be replaced
		String input = null;
		
		// check for highlighted data
		boolean selected = hasSelected();
		
		// If has highlighted data, use the highlighted data
		if(selected){
			input = text.getSelectedText();
		// If no highlighted data, prompt user
		} else {
			input = JOptionPane.showInputDialog("Replace: ");
		}
		
		// If user did not cancel
		if(input != null){
			//ask user for what to replace it with
			System.out.println("Replacing: \""+input+"\"");
			String replacement = JOptionPane.showInputDialog("Replace with: ");
			System.out.println("Replacing with: "+replacement);
			
			// Search for the string to be replaced
			int find = search(input);
			
			// if user did not cancel and the value to be replaced was found
			if(replacement != null && find >= 0) {
				// replace the value
				int start = text.getSelectionStart();
				int end = text.getSelectionEnd();
				String txt = text.getText();
				String newText = txt.substring(0, start) + replacement + txt.substring(end,txt.length());
				text.setText(newText);
				
			// if not found
			} else if(replacement != null && find < 0){
				System.out.println("\""+input+"\"  Not Found");
			}
		// if user canceled the function
		} else {
			System.out.println("Cancelled");
		}		
	}
	
	// Same behavior as replace but replaces ALL targeted words in the text
	public void replace_all(){
		// Variables
		int find = 0;
		String input = null;
		
		// has selected
		boolean selected = hasSelected();
		
		//if there is selected 
		if(selected){
			input = text.getSelectedText();
		// else get user input
		} else {
			input = JOptionPane.showInputDialog("Replace: ");
		}
		
		// if user did not cancel
		if(input != null){
			
			// Get value to replace with
			System.out.println("Replacing: \""+input+"\"\t");
			String replacement = JOptionPane.showInputDialog("Replace with: ");
			System.out.println("Replacing with: "+replacement);
			
			// Find value
			find = search(input);
			
			// If user didn't cancel and replace value is found in text
			if(replacement != null && find >= 0) {
				// While some are still left
				while(find >= 0) {
					int start = text.getSelectionStart();
					int end = text.getSelectionEnd();
					String txt = text.getText();
					String newText = txt.substring(0, start) + replacement + txt.substring(end,txt.length());
					text.setText(newText);
					find = search(input);
				}
			// If user didn't cancel and not initially found
			} else if(replacement != null && find < 0){
				System.out.println("\""+input+"\" Not Found");
			}
		} else {
			System.out.println("Cancelled");
		}	
	}	 
			
	// Prompts the user for a value to find in the text then highlight the text
	public void find(){
		// Prompt user 
		String input = JOptionPane.showInputDialog("Find: ");
		if(input!=null){ 
			System.out.println("Finding: \""+input+"\"");
			// Search the text for the value
			if(search(input) <0) JOptionPane.showMessageDialog(this, "Could not find "+input); // Calls XMenu search
			
		// If user canceled
		} else {
			System.out.println("Cancelled");
		}		
	}
	
	// sets the font
	public void setFontSize(int size){
		text.setFont(new Font("Serif",Font.PLAIN,size)); // Calls XTextFrame to set font
	}
	
}

// END OF DOCUMENT
