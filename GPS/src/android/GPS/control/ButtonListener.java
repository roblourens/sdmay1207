package android.GPS.control;

import android.GPS.view.GPSActivity;
import android.GPS.view.R;
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
		if(v.equals(parent.findViewById(R.id.retrieveButton))){
			Log.d("KLIK", "DER BLEV KLIKKET");
			GPSActivity gps = (GPSActivity) parent;
			gps.clickConnect();
		}
		
	}

}
