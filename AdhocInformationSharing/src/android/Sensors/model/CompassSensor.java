package android.Sensors.model;

import android.Compass.view.R;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.EditText;
import sdmay1207.ais.sensors.Compass;

public class CompassSensor extends Compass implements SensorEventListener
{
	
    private SensorManager sensorManager;
    private Sensor compass;
    private float[] value;
    
    public CompassSensor()
    {
    	sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener((SensorEventListener) this, compass, SensorManager.SENSOR_DELAY_GAME);
      
    }

	@Override
	public CompassReading getReading() 
	{
		return new CompassReading(value[0]);
	}

	@Override
	public String getUnits() 
	{
		return "degrees";
	}
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		 value = event.values;
    }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}


}
