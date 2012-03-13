package android.Compass.control;

import android.Compass.view.CompassActivity;
import android.Compass.view.R;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonListener implements OnClickListener{
	
	Activity parent;

	public ButtonListener(Activity parent) {
		this.parent = parent;
	}
	
	@Override
	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.retrieveButton))){
			CompassActivity gps = (CompassActivity) parent;
			gps.click();
		}
		
	}

}
