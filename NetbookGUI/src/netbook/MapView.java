package netbook;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MapView extends JPanel{

	private static final long serialVersionUID = 1L;
	NetbookGUI parent;
	JButton backBtn;
	
	public MapView(NetbookGUI parent){
		this.parent = parent;
		
		this.add(new JLabel("MAP VIEW NOT IMPLEMENTED"));
		
		backBtn = new JButton("Back");
		backBtn.addActionListener(parent);
		this.add(backBtn);
	}
	
}
