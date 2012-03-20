package sdmay1207.camerastream;

import sdmay1207.android.R;
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
