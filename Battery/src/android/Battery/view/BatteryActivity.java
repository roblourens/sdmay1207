package android.Battery.view;

import android.Battery.control.ButtonListener;
import android.Battery.view.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class BatteryActivity extends Activity {
	Button button;
	IntentFilter ifilter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        ButtonListener listener = new ButtonListener(this);
		button = (Button) findViewById(R.id.getButton);
		button.setOnClickListener(listener);
		
		ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);		
    }
    

	public void showLevel(){
		EditText batteryText = (EditText) findViewById(R.id.batteryText); 
		String output = "> ";
		Intent batteryStatus = registerReceiver(null, ifilter);
		
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		if(isCharging){
			int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
			
			if(usbCharge){
				output += "Battery is Charging by USB";
			} else if(acCharge){
				output += "Battery is Charging by AC";
			} else {
				output += "Battery is Charging";
			}
		} else {
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float batteryPct = level / (float)scale;
			output += "Battery is not Charging: "+batteryPct;
		}
		
		output+="\n";
		batteryText.append(output);
				
	}
}










