package netbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
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
		
	static JButton startStop;
	static JButton receiveBtn;
	static JButton cameraView;
	static JButton mapView;
	static JButton nodeView;
	static JButton nodeInfo;
	
	
	JLabel nodeNum;
	JTextArea textArea;
	
	Color btnColor;
	Font btnFont;
	Font labelFont;
	Font textFont;


	public MainPanel(ActionListener listener, int nodeNumber, Map<Integer, Node> map){
		this.listener = listener;
			
		btnColor = new Color(45, 100, 54);
		btnFont = new Font("Serif", Font.PLAIN, 18);
		labelFont = new Font("Serif", Font.PLAIN, 36);
		textFont = new Font("Serif", Font.PLAIN, 12);
		
	    JPanel selfPanel = createSelfPanel(nodeNumber);
	    JPanel controlPanel = createControlPanel();
	    JPanel textPanel = createTextPanel();
	   
	    this.setLayout(new BorderLayout());
	    this.add(selfPanel, BorderLayout.SOUTH);
	    this.add(controlPanel, BorderLayout.WEST);
	    this.add(textPanel, BorderLayout.EAST);
	    		
		this.setBackground(new Color(36,59,227));
	}

	private JPanel createTextPanel() {
		JPanel retPanel = new JPanel();
		
		textArea = new JTextArea(50, 40);
		textArea.setBackground(Color.WHITE);
		textArea.setFont(textFont);
		retPanel.add(textArea);
		
		return retPanel;
	}

	private JPanel createControlPanel() {
		JPanel retPanel = new JPanel();
	    startStop = new JButton("Start");
	    startStop.setFont(btnFont);
	    startStop.setBackground(btnColor);
	    startStop.addActionListener(listener);
	    
	    mapView = new JButton("Map View");
	    mapView.setBackground(btnColor);
	    mapView.setFont(btnFont);
	    mapView.addActionListener(listener);
	    
	    nodeView = new JButton("Node List View");
	    nodeView.setBackground(btnColor);
	    nodeView.setFont(btnFont);
	    nodeView.addActionListener(listener);
	    
	    
	    cameraView = new JButton("Get Camera Footage");
	    cameraView.setBackground(btnColor);
	    cameraView.setFont(btnFont);
	    cameraView.addActionListener(listener);
	    

	    retPanel.setLayout(new GridLayout(4,1));
	    retPanel.add(startStop);
	    retPanel.add(mapView);
	    retPanel.add(nodeView);
	    
		return retPanel;
	}

	private JPanel createSelfPanel(int nodeNumber) {
		JPanel retPanel = new JPanel();
	    nodeNum = new JLabel("Node Number: "+nodeNumber);
	    nodeNum.setFont(labelFont);
	    nodeNum.setForeground(btnColor);
	    
	    nodeInfo = new JButton("This Node Info");
		nodeInfo.setBackground(btnColor);
		nodeInfo.setFont(btnFont);
	    nodeInfo.addActionListener(listener);
	    
	    retPanel.setLayout(new GridLayout(1,2));
	    retPanel.add(nodeNum);
	    retPanel.add(nodeInfo);
	    
		return retPanel;
	}



}
