package netbook;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import sdmay1207.ais.network.model.Node;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	ActionListener listener;
		
	JButton startStop;
	JButton receiveBtn;
	JButton camView;
	//JButton mapView;
	JButton nodeView;
	JButton nodeInfo;
	
	
	JLabel nodeNum;
	JTextArea textArea;
	TextPanel textPanel;
	
	Color btnColor;
	Font btnFont;
	Font labelFont;
	Font textFont;


	public MainPanel(NetbookGUI parent, int nodeNumber, Map<Integer, Node> map){
		this.listener = (ActionListener) parent;
			
		btnColor = new Color(45, 100, 54);
		btnFont = new Font("Serif", Font.PLAIN, 18);
		labelFont = new Font("Serif", Font.PLAIN, 36);
		textFont = new Font("Serif", Font.PLAIN, 12);
		
	    JPanel selfPanel = createSelfPanel(nodeNumber);
	    JPanel controlPanel = createControlPanel();
	    JPanel textPanel = createTextPanel(parent, nodeNumber);
	   
	    this.setLayout(new BorderLayout());
	    this.add(selfPanel, BorderLayout.SOUTH);
	    this.add(controlPanel, BorderLayout.NORTH);
	    this.add(textPanel, BorderLayout.CENTER);
	    		
		this.setBackground(Color.GRAY);
	}

	private JPanel createTextPanel(NetbookGUI parent, int nodeNum) {
		textPanel = new TextPanel(parent, nodeNum);
		return textPanel;
	}

	private JPanel createControlPanel() {
		JPanel retPanel = new JPanel();
	    startStop = new JButton("Start");
	    startStop.setSize(20,20);
	    startStop.setFont(btnFont);
	    startStop.setBackground(btnColor);
	    startStop.addActionListener(listener);
	    
	    //mapView = new JButton("Map View");
	    //mapView.setBackground(btnColor);
	    //mapView.setFont(btnFont);
	    //mapView.addActionListener(listener);
	    
	    nodeView = new JButton("Node List View");
	    nodeView.setBackground(btnColor);
	    nodeView.setFont(btnFont);
	    nodeView.addActionListener(listener);
	    
	    
	    camView = new JButton("Get Camera Footage");
	    camView.setBackground(btnColor);
	    camView.setFont(btnFont);
	    camView.addActionListener(listener);
	    

	    //retPanel.setLayout(new GridLayout(4,1));
	    retPanel.add(startStop);
	   // retPanel.add(mapView);
	    retPanel.add(nodeView);
	    retPanel.add(camView);
	    
		return retPanel;
	}

	private JPanel createSelfPanel(int nodeNumber) {
		JPanel retPanel = new JPanel();
	    nodeNum = new JLabel("Node Number: "+nodeNumber);
	    nodeNum.setFont(labelFont);
	    nodeNum.setForeground(btnColor);
	    
	    nodeInfo = new JButton("Get This Node Info");
		nodeInfo.setBackground(btnColor);
		nodeInfo.setFont(btnFont);
	    nodeInfo.addActionListener(listener);
	    
	    //retPanel.setLayout(new BoxLayout(retPanel, BoxLayout.IS));
	    retPanel.add(nodeNum);
	    retPanel.add(nodeInfo);
	    
		return retPanel;
	}

	public void displayMessage(String msg){
		textPanel.addMessage(msg);
	}
	
	public void displayMessage(int srcNode, int destNode, String msg){
		textPanel.addMessage(srcNode, destNode, msg);
	}

}
