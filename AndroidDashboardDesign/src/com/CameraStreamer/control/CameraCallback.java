package com.CameraStreamer.control;

import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import com.androidhive.dashboard.CameraActivity;

public class CameraCallback implements Callback {
	
	CameraActivity parent;
	
	public CameraCallback(CameraActivity parent){
		this.parent = parent;
	}
	
	//@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	//@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	//@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		parent.stopStreaming();
	}

}
