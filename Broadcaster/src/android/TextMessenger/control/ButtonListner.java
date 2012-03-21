package android.TextMessenger.control;

import android.TextMessenger.view.R;
import android.TextMessenger.view.Broadcast;
import android.TextMessenger.view.Connect;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonListner implements OnClickListener{
	Activity parent;

	public ButtonListner(Activity parent) {
		this.parent = parent;
	}

	@Override
	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.connectButton))){
			Log.d("KLIK", "DER BLEV KLIKKET");
			Connect c = (Connect)parent;
			c.clickConnect();
		
		}
		else if(v.equals(parent.findViewById(R.id.sendButton))){
			((Broadcast) parent).sendMessage("text");
		}
		else if(v.equals(parent.findViewById(R.id.helloButton))){
			((Broadcast) parent).sendMessage("hello");
		}
		else if(v.equals(parent.findViewById(R.id.gpsButton))){
			((Broadcast) parent).sendGPS();
		}
		
	}
}
