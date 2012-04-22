package netbook.node;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import netbook.textmessenger.InsetTextPanel;
import netbook.NodeView;
import netbook.textmessenger.TextMessengerListener;


public class NodePanel extends JPanel implements ActionListener, Observer, TextMessengerListener{

	private static final long serialVersionUID = 1L;

	NodeView parent;
	Node node;
	InsetTextPanel textPanel;
	
	JLabel nodeInfo;
	JLabel latLong;
	JLabel batteryStatus;
	JLabel compassRead;
	JLabel tasking;
	JLabel connectStatus;
	
	String batteryLine = "BATTERY STATUS: ";
	String compassLine = "COMPASS READING: ";
	String gpsLine = "GPS READING (Lat, Long): ";
	String connectionLine = "CONNECTION: ";
	String taskingLine = "TASKING: ";
	
	JButton closeBtn;
	JButton mapBtn;
	JButton textBtn;
	
	Font titleFont;
	Font regularFont;
	
	public NodePanel(Node node, NodeView parent){
		this.parent = parent;
		this.node = node;
		node.addObserver(this);
				
		titleFont = new Font("Serif", Font.PLAIN, 18);
		regularFont = new Font("Serif", Font.PLAIN, 12);
		
		nodeInfo = new JLabel("Node "+node.getNodeNumber());
		nodeInfo.setFont(titleFont);
		nodeInfo.setForeground(Color.WHITE);
			
		latLong = new JLabel(getLatLong());
		latLong.setFont(regularFont);
		latLong.setForeground(Color.WHITE);
			
		compassRead = new JLabel(getCompass());
		compassRead.setFont(regularFont);
		compassRead.setForeground(Color.WHITE);

		batteryStatus = new JLabel(getBattery());
		batteryStatus.setFont(regularFont);
		batteryStatus.setForeground(Color.WHITE);
			
		tasking = new JLabel(getTasking());
		tasking.setFont(regularFont);
		tasking.setForeground(Color.WHITE);
		
		connectStatus = new JLabel(getConnection());
		connectStatus.setFont(regularFont);
		connectStatus.setForeground(Color.WHITE);
		
		closeBtn = new JButton("Close");
		closeBtn.setFont(regularFont);
		closeBtn.setBackground(Color.WHITE);
		closeBtn.addActionListener(this);
			
		textBtn = new JButton("Text this node");
		textBtn.setFont(regularFont);
		textBtn.setBackground(Color.WHITE);
		textBtn.addActionListener(this);

		mapBtn = new JButton("View the map");
		mapBtn.setFont(regularFont);
		mapBtn.setBackground(Color.WHITE);
		mapBtn.addActionListener(this);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBackground(Color.BLACK);
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));	
		dataPanel.add(nodeInfo);
		dataPanel.add(closeBtn);
		dataPanel.add(Box.createRigidArea(new Dimension(0,20)));
		dataPanel.add(latLong);
		dataPanel.add(Box.createRigidArea(new Dimension(0,10)));
		dataPanel.add(batteryStatus);
		dataPanel.add(Box.createRigidArea(new Dimension(0,10)));
		dataPanel.add(compassRead);
		dataPanel.add(Box.createRigidArea(new Dimension(0,10)));
		dataPanel.add(tasking);
		dataPanel.add(Box.createRigidArea(new Dimension(0,10)));
		dataPanel.add(connectStatus);
		dataPanel.add(mapBtn);
		dataPanel.add(textBtn);
		
		this.setLayout(new BorderLayout());
		this.add(dataPanel, BorderLayout.CENTER);
	}

	
	
	public void update(Observable obj, Object arg){
		System.out.println("Updating Node: "+node.getNodeNumber());
		
		connectStatus.setText(getConnection());	
		latLong.setText(getLatLong());
		batteryStatus.setText(getBattery());
		compassRead.setText(getCompass());
		tasking.setText(getTasking());
	}

	
	
	
	public int getNodeNumber(){
		return node.getNodeNumber();
	}
		
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == closeBtn){
			parent.closeNode(node.getNodeNumber());
			
		} else if(action.getSource() == textBtn){
			this.openTextMessenger(node.getNodeNumber());
			
		} else if(action.getSource() == mapBtn){
			parent.openMap(node.getNodeNumber());
			
		}
	}	
	
	
	
	
	private String getLatLong(){
		return gpsLine+node.getGPSReading();
	}

	private String getBattery(){
		return batteryLine+node.getBatteryStatus();
	}
	
	private String getCompass(){
		return compassLine+node.getCompassReading();		
	}
	
	private String getTasking(){
		return taskingLine+node.getTasking();
	}
	
	private String getConnection(){
		return connectionLine+node.getConnection();
	}



	
	
	
	@Override
	public void sendMessage(int number, String message) {
		parent.sendMessage(number, message, node.getNodeNumber());
	}



	
	@Override
	public void sendMessageToAll(String message) {
		parent.sendMessageToAll(message, node.getNodeNumber());	
	}



	
	@Override
	public void closeTextMessenger() {
		textPanel.setVisible(false);
		textPanel = null;
	}



	
	@Override
	public void openTextMessenger(int nodeNum) {
		textPanel = new InsetTextPanel(this, nodeNum);
		this.add(textPanel, BorderLayout.EAST);
		textPanel.setDestination(nodeNum);
		this.updateUI();
	}
}