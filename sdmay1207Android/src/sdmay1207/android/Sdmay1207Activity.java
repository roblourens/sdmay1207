package sdmay1207.android;

import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.Device;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;
import sdmay1207.camerastream.CameraView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Sdmay1207Activity extends Activity implements Observer
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final NodeController nc = ((Sdmay1207Application) getApplication()).nc;
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));
        nc.addNetworkObserver(this);

        ((Button) findViewById(R.id.startButton))
                .setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Device.sysCommand("su -c \"/data/data/android.tether/bin/tether stop 1\"");
                        nc.start(RoutingAlg.AODV);
                    }
                });

        ((Button) findViewById(R.id.endButton))
                .setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        nc.stop();
                        Device.sysCommand("su -c \"/data/data/android.tether/bin/tether stop 1\"");
                    }
                });

        final Context c = this;
        ((Button) findViewById(R.id.cameraButton))
                .setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (!nc.isRunning())
                            Toast.makeText(c, "You must press start first", 4)
                                    .show();
                        else
                        {
                            Intent i = new Intent(c, CameraView.class);
                            startActivity(i);
                        }
                    }
                });
    }

    @Override
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event)
        {
        case RecvdHeartbeat:
            Heartbeat hb = (Heartbeat) netEvent.data;
            System.out.println("Got heartbeat from " + hb.from + ": "
                    + hb.toString());
            // do something useful with it- pass to GUI or something
            // **or GUI has actually registered as the listener** this
            break;
        case NodeJoined:
            System.out.println("Node joined: " + netEvent.data);
            break;
        case NodeLeft:
            System.out.println("Node left: " + netEvent.data);
            break;
        }
    }
}