package netbook;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.sensors.Battery.BatteryStatus;
import sdmay1207.ais.sensors.Compass.CompassReading;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class NodePanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	int nodeNum;
	Heartbeat heartbeat;
	NodeView parent;
	
	JLabel nodeInfo;
	JLabel latLong;
	JLabel batteryStatus;
	JLabel compassRead;
	JLabel tasking;
	
	JButton closeBtn;
	
	Font titleFont;
	Font regularFont;
	
	public NodePanel(Heartbeat heartbeat, int nodeNum, NodeView parent){
		this.parent = parent;
		this.heartbeat = heartbeat;
		this.nodeNum = nodeNum;
			
		titleFont = new Font("Serif", Font.PLAIN, 18);
		regularFont = new Font("Serif", Font.PLAIN, 12);
		
		nodeInfo = new JLabel("Node "+nodeNum);
		nodeInfo.setFont(titleFont);
		nodeInfo.setForeground(Color.WHITE);
			
		latLong = new JLabel(getGPSReading());
		latLong.setFont(regularFont);
		latLong.setForeground(Color.WHITE);
			
		compassRead = new JLabel(getCompassReading());
		compassRead.setFont(regularFont);
		compassRead.setForeground(Color.WHITE);

		batteryStatus = new JLabel(getBatteryStatus());
		batteryStatus.setFont(regularFont);
		batteryStatus.setForeground(Color.WHITE);
			
		tasking = new JLabel(getTasking());
		tasking.setFont(regularFont);
		tasking.setForeground(Color.WHITE);
		
		closeBtn = new JButton("Close");
		closeBtn.setFont(regularFont);
		closeBtn.setBackground(Color.WHITE);
		closeBtn.addActionListener(this);
			
		this.setBackground(Color.BLACK);
		//this.setLayout(new GridLayout(0,1));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		this.add(nodeInfo);
		this.add(closeBtn);
		this.add(Box.createRigidArea(new Dimension(0,20)));
		this.add(latLong);

		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(batteryStatus);

		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(compassRead);

		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(tasking);
	}

	
	private String getTasking() {	
		String taskingLine = "TASKING: ";
		return taskingLine + "No data";
	}

	private String getBatteryStatus() {
		String batteryLine = "BATTERY STATUS: ";
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.Battery) != null){
			BatteryStatus bs= new BatteryStatus(heartbeat.sensorOutput.get(SensorType.Battery));
			return batteryLine+bs.toString();
		}
		return batteryLine+"No data";
	}

	private String getCompassReading() {
		String compassLine = "COMPASS READING: ";
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.Compass) != null){
			CompassReading cr = new CompassReading(heartbeat.sensorOutput.get(SensorType.Compass));
			return compassLine+cr.getReading()+"";
		}
		return compassLine + "No data";
	}

	private String getGPSReading() {	
		String gpsLine = "GPS READING (Lat, Long): ";
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.GPS) != null){
			Location loc = new Location(heartbeat.sensorOutput.get(SensorType.GPS));
			return gpsLine + loc.toString();
		}
		return gpsLine + "No data";
	}

	
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == closeBtn){
			parent.closeTab(nodeNum);
		}
	}

	
	public void newHeartbeat(Heartbeat hb) {
		heartbeat = hb;
		
		latLong.setText(getGPSReading());
		batteryStatus.setText(getBatteryStatus());
		compassRead.setText(getCompassReading());
		tasking.setText(getTasking());
	}
}
