package netbook.node;

import java.util.Observable;

import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.sensors.Battery.BatteryStatus;
import sdmay1207.ais.sensors.Compass.CompassReading;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class Node extends Observable{
	
	Heartbeat heartbeat;
	int nodeNum;
	boolean connected;
	
	
	public Node(int nodeNum){
		this.nodeNum = nodeNum;
		connected = false;
		heartbeat = null;
	}
	
	
	
	public int getNodeNumber(){
		return nodeNum;
	}
	
	public String getTasking() {	
		return "No data";
	}

	public String getBatteryStatus() {
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.Battery) != null){
			BatteryStatus bs= new BatteryStatus(heartbeat.sensorOutput.get(SensorType.Battery));
			return bs.toString()+"%";
		}
		return "No data";
	}

	public String getCompassReading() {
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.Compass) != null){
			CompassReading cr = new CompassReading(heartbeat.sensorOutput.get(SensorType.Compass));
			return cr.getReading()+"";
		}
		return "No data";
	}

	public String getGPSReading() {	
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.GPS) != null){
			Location loc = new Location(heartbeat.sensorOutput.get(SensorType.GPS));
			return loc.toString();
		}
		return "No data";
	}
	
	public String getConnection(){
		if(connected) return "Connected";
		else return "Disconnected";
	}
	
	
	public float getLongitude(){
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.GPS) != null){
			Location loc = new Location(heartbeat.sensorOutput.get(SensorType.GPS));
			return (float) loc.longitude;
		}
		return (float) -93.647;
	}
	
	public float getLatitude(){
		if(heartbeat != null && heartbeat.sensorOutput.get(SensorType.GPS) != null){
			Location loc = new Location(heartbeat.sensorOutput.get(SensorType.GPS));
			return (float) loc.latitude;
		}
		return (float) 42.027950;
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	public void setConnection(boolean connected){
		this.connected = connected;
	}
	
	
	public void newHeartbeat(Heartbeat hb) {
		heartbeat = hb;
		//System.out.println("Received new heartbeat, updating observers: "+this.countObservers());
		this.setChanged();
		this.notifyObservers(heartbeat);
	}


}
