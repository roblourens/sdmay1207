package android.Battery.control;

import android.Battery.view.BatteryActivity;
import android.Battery.view.R;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonListener implements OnClickListener{
	
	Activity parent;

	public ButtonListener(Activity parent) {
		this.parent = parent;
	}
	
	@Override
	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.getButton))){
			Log.d("KLIK", "Clicked Get Button");
			BatteryActivity battery = (BatteryActivity) parent;
			battery.showLevel();
		}
		
	}

}
