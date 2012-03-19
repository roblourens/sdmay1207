package com.androidhive.dashboard;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Html;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidhive.dashboard.R;

import com.CameraStreamer.control.ButtonListener;
import com.CameraStreamer.control.CameraCallback;
import com.CameraStreamer.model.CameraStreamer;

public class PhotosActivity extends Activity {
	
	// Variables for layout
	public Button startButton;
	public TextView console;
	private EditText ipText;
	   
	// Variables for camera 
	private SurfaceView camera;
	private PowerManager.WakeLock wl;
	private int resX, resY, fps;
	private static CameraStreamer streamer;
	    
	// Log Tag
	static final public String LOG_TAG = "CameraStreamer";
	
	
	
	// Called when application is first created
	public void onCreate(Bundle savedInstanceState) {
	    	
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.photos_layout);
	        
	     // Get View
	     camera = (SurfaceView)findViewById(R.id.smallcameraview);
	     startButton = (Button) findViewById(R.id.streambutton);
	     console = (TextView) findViewById(R.id.console);
	     ipText = (EditText) findViewById(R.id.ip);
	            
	     // Set button listener
	     startButton.setOnClickListener(new ButtonListener(this));
	   
	     // Set holders
	     camera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	     camera.getHolder().addCallback(new CameraCallback(this));
	        
	     // Power Manager
	     PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	     wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "CameraViewWakeLock");
	     
	     // Create Streamer
	     streamer = new CameraStreamer();
	    	
	 }
	    
	// Called when application is destroyed
	public void onDestroy(){
		 Log.d(LOG_TAG,"Destorying...");
		 streamer.destroy();
		 super.onDestroy();
	}
	
	// Called when application resumes
	public void onResume(){
		
		 super.onResume();
		 console.setText("");
		 log("<b>Welcome</b>");
	}
	
	// Called when user clicks stream button
	// Turns streaming on/off
	public void toggleStreaming() {
		Log.d(LOG_TAG,"Toggling");
		if (streamer.isStreaming())
			stopStreaming();
		else
			startStreaming();
	}
    
	// Called when streaming starts
	private void startStreaming() {
    	
		// Video resolution
		resX = 176;
    	resY = 144;
	    fps =  5;

	    // Get IP from user/view
	    String ip = ipText.getText().toString();
    	log("Sending to '"+ip+"'");
    	Log.d(LOG_TAG,"Sending to "+ip);
	    	    
    	// If already streaming, return
    	if (streamer.isStreaming()) return;
    	
    	// Try to setup camera
		try {
			streamer.setup(camera.getHolder(),ip, resX, resY, fps);
		} catch (IOException e) {
			log(e.getMessage());
			return;
		}

		// Start streamer and establish running environment
		streamer.start();
		ipText.setEnabled(false);
		startButton.setText("Stop");
		wl.acquire();
		Log.d(LOG_TAG,"Started Streaming");
		this.log("Streaming Started");	
	}
     
	public void stopStreaming() {
    	
		// If not streaming, then nothing to do
		if (!streamer.isStreaming()) return;
    	
		// Stop streamer and destroy it, set up stopped environment
		streamer.stop();
		streamer.destroy();
		startButton.setText("Stream");
		ipText.setEnabled(true);
		wl.release();
		
		Log.d(LOG_TAG,"Stopped Streaming");
		this.log("Streaming Stopped");
    }
    
   
	// Outputs information to the screen in html format
	public void log(String s) {
		console.append(Html.fromHtml(s+"<br />"));
    }
    
    
}
