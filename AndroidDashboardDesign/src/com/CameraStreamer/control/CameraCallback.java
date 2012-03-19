package com.CameraStreamer.control;

import com.androidhive.dashboard.*;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class CameraCallback implements Callback {
	
	PhotosActivity parent;
	
	public CameraCallback(PhotosActivity parent){
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
