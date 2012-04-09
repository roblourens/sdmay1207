package com.CameraStreamer.control;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidhive.dashboard.R;

import com.androidhive.dashboard.CameraActivity;


public class ButtonListener implements OnClickListener{
	Activity parent;

	public ButtonListener(Activity parent) {
		this.parent = parent;
	}

	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.streambutton))){
			Log.d(CameraActivity.LOG_TAG,"StreamButton Clicked");
			((CameraActivity) parent).toggleStreaming();
		}
	}
}
