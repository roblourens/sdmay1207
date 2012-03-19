package netbook;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.sensors.Battery.BatteryStatus;
import sdmay1207.ais.sensors.Compass.CompassReading;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class NodePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	int nodeNum;
	Heartbeat heartbeat;
	
	JLabel nodeInfo;
	JLabel latitude;
	JLabel longitude;
	JLabel batteryStatus;
	JLabel compassRead;
	JLabel tasking;
	
	Font titleFont;
	Font regularFont;
	
	public NodePanel(Heartbeat heartbeat, int nodeNum){
		if(heartbeat==null){
			this.nodeNum = nodeNum;
			
			titleFont = new Font("Serif", Font.PLAIN, 18);
			regularFont = new Font("Serif", Font.PLAIN, 12);
			
			nodeInfo = new JLabel("Node "+nodeNum);
			nodeInfo.setFont(titleFont);
			nodeInfo.setForeground(Color.WHITE);
			
			latitude = new JLabel("No data for Latitude");
			longitude = new JLabel("No data for Longitude");
			batteryStatus = new JLabel("No data for Battery");
			compassRead = new JLabel("No data for Compass");
			tasking = new JLabel("No data for tasking");
			
			this.setBackground(Color.BLACK);
			this.setLayout(new GridLayout(0,1));
			
			this.add(nodeInfo);
			this.add(latitude);
			this.add(longitude);
			this.add(batteryStatus);
			this.add(compassRead);
			this.add(tasking);
			
			this.add(nodeInfo);
		} else {
			this.heartbeat = heartbeat;
			this.nodeNum = nodeNum;
			
			titleFont = new Font("Serif", Font.PLAIN, 18);
			regularFont = new Font("Serif", Font.PLAIN, 12);
			
			nodeInfo = new JLabel("Node "+nodeNum);
			nodeInfo.setFont(titleFont);
			nodeInfo.setForeground(Color.WHITE);
			
			getGPS();
			getBatterStatus();
			getCompassReading();
			getTasking();
			
			this.setBackground(Color.BLACK);
			this.setLayout(new GridLayout(0,1));
			
			this.add(nodeInfo);
			this.add(latitude);
			this.add(longitude);
			this.add(batteryStatus);
			this.add(compassRead);
			this.add(tasking);
			
			this.add(nodeInfo);
		}
	}

	private void getTasking() {	
		tasking = new JLabel("No data for tasking");
	}

	private void getBatterStatus() {
		BatteryStatus bs= new BatteryStatus(heartbeat.sensorOutput.get(SensorType.Battery));
		batteryStatus = new JLabel(bs.toString());
		batteryStatus.setFont(regularFont);
		batteryStatus.setForeground(Color.WHITE);
	}

	private void getCompassReading() {
		CompassReading cr = new CompassReading(heartbeat.sensorOutput.get(SensorType.Compass));
		compassRead = new JLabel("Compass Reading: "+cr.getReading());
		compassRead.setFont(regularFont);
		compassRead.setForeground(Color.WHITE);
	}

	private void getGPS() {		
		Location loc = new Location(heartbeat.sensorOutput.get(SensorType.GPS));
		
		latitude = new JLabel("Latitude: "+loc.latitude);
		latitude.setFont(regularFont);
		latitude.setForeground(Color.WHITE);
		
		longitude = new JLabel("Longitude: "+loc.longitude);
		longitude.setFont(regularFont);
		longitude.setForeground(Color.WHITE);
	}
}
