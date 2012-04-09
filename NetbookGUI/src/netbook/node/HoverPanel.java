package netbook.node;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import netbook.MapView;

public class HoverPanel extends JPanel implements Observer, ActionListener{

	private static final long serialVersionUID = 1L;

	Node node;
	MapView parent;
	
	JLabel nodeInfo;
	JLabel latLong;
	JLabel batteryStatus;
	JLabel compassRead;
	JLabel tasking;
	JLabel connectStatus;
	
	JButton nodeViewerBtn;
	JButton sendTextMsgBtn;
	
	String batteryLine = "BATTERY: ";
	String compassLine = "COMPASS: ";
	String gpsLine = "GPS(Lat, Long): ";
	String connectionLine = "CONNECTION: ";
	String taskingLine = "TASK: ";
	
	Font titleFont;
	Font regularFont;
	
	public HoverPanel(Node node, MapView parent){
		this.node = node;
		this.parent = parent;
		node.addObserver(this);
				
		titleFont = new Font("Serif", Font.PLAIN, 12);
		regularFont = new Font("Serif", Font.PLAIN, 10);
		
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
		
		nodeViewerBtn = new JButton("Node View");
		nodeViewerBtn.setFont(regularFont);
		nodeViewerBtn.setBackground(Color.WHITE);
		nodeViewerBtn.addActionListener(this);
		
		sendTextMsgBtn = new JButton("Text Message");
		sendTextMsgBtn.setFont(regularFont);
		sendTextMsgBtn.setBackground(Color.WHITE);
		sendTextMsgBtn.addActionListener(this);
		
		JPanel buttons = new JPanel();
		buttons.setBackground(new Color(16, 150, 70));
		buttons.add(nodeViewerBtn);
		buttons.add(sendTextMsgBtn);
		
		this.setBackground(Color.BLACK);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		this.add(nodeInfo);
		this.add(latLong);
		this.add(batteryStatus);
		this.add(compassRead);
		this.add(tasking);
		this.add(connectStatus);
		this.add(buttons);
		
	}

	
	
	public void update(Observable obj, Object arg){
		//System.out.println("Updating Node: "+node.getNodeNumber());
		
		connectStatus.setText(getConnection());	
		latLong.setText(getLatLong());
		batteryStatus.setText(getBattery());
		compassRead.setText(getCompass());
		tasking.setText(getTasking());
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



	
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == sendTextMsgBtn){
			parent.openTextMessenger(node.getNodeNumber());
		} else if(action.getSource() == nodeViewerBtn){
			parent.openNodeViewer(node.getNodeNumber());
		}
	}
}

