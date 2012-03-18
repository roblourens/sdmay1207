package android.CameraStreamer.control;

import android.CameraStreamer.view.R;
import android.CameraStreamer.view.CameraView;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ButtonListener implements OnClickListener{
	Activity parent;

	public ButtonListener(Activity parent) {
		this.parent = parent;
	}

	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.streambutton))){
			Log.d(CameraView.LOG_TAG,"StreamButton Clicked");
			((CameraView) parent).toggleStreaming();
		}
	}
}
