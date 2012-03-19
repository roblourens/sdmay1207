package com.CameraStreamer.control;

import com.androidhive.dashboard.PhotosActivity;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidhive.dashboard.R;


public class ButtonListener implements OnClickListener{
	Activity parent;

	public ButtonListener(Activity parent) {
		this.parent = parent;
	}

	public void onClick(View v) {
		if(v.equals(parent.findViewById(R.id.streambutton))){
			Log.d(PhotosActivity.LOG_TAG,"StreamButton Clicked");
			((PhotosActivity) parent).toggleStreaming();
		}
	}
}
