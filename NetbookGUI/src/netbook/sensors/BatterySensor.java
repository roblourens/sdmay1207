package netbook.sensors;


import java.io.IOException;
import java.io.InputStream;

import sdmay1207.ais.sensors.Battery;

public class BatterySensor extends Battery{

	@Override
	public BatteryStatus getReading() {
		try {
           byte[] buf = new byte[100];
           boolean charging = false;
           int percent = -1;
           Process process = Runtime.getRuntime().exec("acpi");
           InputStream in = process.getInputStream();
           
           in.read(buf, 0, 100);
           String output = new String(buf);
           
           if(output.indexOf("Unknown")>0){
        	   charging = false;
           } else {
        	   charging = true;
           }
           
    	   int percentSign = output.indexOf("%");
    	   int space = output.substring(0, percentSign).lastIndexOf(' ');
    	   percent = Integer.parseInt(output.substring(space,percentSign));
           
           return new BatteryStatus(percent, charging);
        } catch(IOException e){
        	e.printStackTrace();
        }
		
		return null;
	}

	@Override
	public String getUnits() {
		return "%";
	}

}
