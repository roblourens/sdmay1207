package sdmay1207.camerastream;

import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class CameraCallback implements Callback {
	
	CameraView parent;
	
	public CameraCallback(CameraView parent){
		this.parent = parent;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		parent.stopStreaming();
	}

}
