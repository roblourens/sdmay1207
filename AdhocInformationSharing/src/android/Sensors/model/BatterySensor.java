package android.Sensors.model;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import sdmay1207.ais.sensors.Battery;

public class BatterySensor extends Battery 
{

	private IntentFilter ifilter;
	
	public BatterySensor()
	{
		ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);	
	}
	
	@Override
	public BatteryStatus getReading() 
	{
		Intent batteryStatus = registerReceiver(null, ifilter);
		
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryPct = level / (float)scale;
		
		return new BatteryStatus(batteryPct, isCharging);
		
	}

	@Override
	public String getUnits() 
	{
		return "%";
	}

}
