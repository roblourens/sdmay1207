package sdmay1207.camerastream;

import java.io.IOException;

import sdmay1207.ais.network.NetworkController;
import sdmay1207.android.R;
import sdmay1207.android.Sdmay1207Application;
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

public class CameraView extends Activity
{

    // Variables for layout
    public Button startButton;
    public TextView console;
    private EditText ipText;

    // Variables for camera
    private SurfaceView cameraView;
    private PowerManager.WakeLock wl;
    private int resX, resY, fps;
    private static CameraStreamer streamer;

    // Log Tag
    static final public String LOG_TAG = "CameraStreamer";

    // Called when application is first created
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        // Get View
        cameraView = (SurfaceView) findViewById(R.id.smallcameraview);
        startButton = (Button) findViewById(R.id.streambutton);
        console = (TextView) findViewById(R.id.console);
        ipText = (EditText) findViewById(R.id.ip);

        // Set button listener
        startButton.setOnClickListener(new ButtonListener(this));

        // Set holders
        cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraView.getHolder().addCallback(new CameraCallback(this));

        // Power Manager
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "CameraViewWakeLock");

        // Create Streamer
        streamer = new CameraStreamer();
    }

    // Called when application is destroyed
    public void onDestroy()
    {
        Log.d(LOG_TAG, "Destroying...");
        streamer.destroy();
        super.onDestroy();
    }

    // Called when application resumes
    public void onResume()
    {

        super.onResume();
        console.setText("");
        log("<b>Welcome</b>");
    }

    // Called when user clicks stream button
    // Turns streaming on/off
    public void toggleStreaming()
    {
        Log.d(LOG_TAG, "Toggling");
        if (streamer.isStreaming())
            stopStreaming();
        else
            startStreaming();
    }

    // Called when streaming starts
    private void startStreaming()
    {
        // Video resolution
        resX = 176;
        resY = 144;
        fps = 5;

        // Get IP from user/view
        String ip = ipText.getText().toString();
        log("Sending to '" + ip + "'");
        Log.d(LOG_TAG, "Sending to " + ip);

        // If already streaming, return
        if (streamer.isStreaming())
            return;

        // Try to setup camera
        try
        {
            NetworkController netController = ((Sdmay1207Application) getApplication()).nc.networkController;
            streamer.setup(cameraView.getHolder(), ip, resX, resY, fps,
                    netController);
        } catch (IOException e)
        {
            log(e.getMessage());
            return;
        }

        // Start streamer and establish running environment
        streamer.start();
        ipText.setEnabled(false);
        startButton.setText("Stop");
        wl.acquire();
        Log.d(LOG_TAG, "Started Streaming");
        this.log("Streaming Started");
    }

    public void stopStreaming()
    {

        // If not streaming, then nothing to do
        if (!streamer.isStreaming())
            return;

        // Stop streamer and destroy it, set up stopped environment
        streamer.stop();
        streamer.destroy();
        startButton.setText("Stream");
        ipText.setEnabled(true);
        wl.release();

        Log.d(LOG_TAG, "Stopped Streaming");
        this.log("Streaming Stopped");
    }

    // Outputs information to the screen in html format
    public void log(String s)
    {
        console.append(Html.fromHtml(s + "<br />"));
    }

}
