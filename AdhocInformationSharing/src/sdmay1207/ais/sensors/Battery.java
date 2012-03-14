package sdmay1207.ais.sensors;

import sdmay1207.ais.sensors.SensorInterface.SensorType;

public abstract class Battery extends Sensor
{

	public Battery() 
	{
		super(SensorType.Battery);
	}
	
	
	public abstract BatteryStatus getReading();
	
	public class BatteryStatus
	{
		private boolean charging;
		private float percentage;
		
		public BatteryStatus(float percentage, boolean charging)
		{
			this.charging = charging;
			this.percentage = percentage;
		}
		
		
		public float getPercentage()
		{
			return percentage;
		}
		
		
		public boolean isCharging()
		{
			return charging;
		}
	}

}
