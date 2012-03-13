package android.Compass.view;

import android.Compass.control.ButtonListener;
import android.Compass.view.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class CompassActivity extends Activity implements SensorEventListener{
    
    private SensorManager sensorManager;
    private Sensor compass;
    private float[] value;
    Button button;
    String orientString;
	
    public final String TAG = "Compass";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ButtonListener listener = new ButtonListener(this);
		button = (Button) findViewById(R.id.retrieveButton);
		button.setOnClickListener(listener);
        
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener((SensorEventListener) this, compass, SensorManager.SENSOR_DELAY_GAME);
        
        
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		 Log.d(TAG,"sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
         value = event.values;
         EditText text = (EditText) findViewById(R.id.output); 
         text.setText(value[0]+"   "+value[1]+"   "+value[2]+"\n");
    }

	public void click(){
		EditText text = (EditText) findViewById(R.id.saved); 
		getOrientation();
		text.append("> "+orientString+":"+value[0]+"\n");
	}
	
	public void getOrientation(){
		int orient = (int) value[0]-22;
		String orientStr = "";
		int count = 0;
		
		while(orient > 0){
			count +=1;
			orient -= 45;
		}
		
		
		switch(count){
			case 0	: orientStr = "N"; 	break;
			case 1	: orientStr = "NE"; break;
			case 2	: orientStr = "E"; 	break;
			case 3	: orientStr = "SE"; break;
			case 4	: orientStr = "S"; 	break;
			case 5	: orientStr = "SW"; break;
			case 6	: orientStr = "W"; 	break;
			case 7	: orientStr = "NW"; break;
			case 8	: orientStr = "N"; 	break;
			default	: break;
		}
		orientString = orientStr;
	}

}