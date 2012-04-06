package com.androidhive.dashboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.Battery.BatteryStatus;
import sdmay1207.ais.sensors.Compass.CompassReading;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidhive.dashboard.R;

public class NodeDetailsActivity extends Activity implements Observer
{
    public static final String NODE_NUM_KEY = "nodenum";

    private NodeController nc;
    private Node displayedNode;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_details);

        nc = ((DashboardApplication) getApplication()).nc;

        final int nodeNum = getIntent().getIntExtra(NODE_NUM_KEY, 0);
        displayedNode = nc.getKnownNodes().get(nodeNum);

        if (displayedNode.lastHeartbeat != null)
            updateInterfaceWithHeartbeat(displayedNode.lastHeartbeat);

        // Set text button listener
        final Context c = this;
        ((Button) findViewById(R.id.sendTextButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent i = new Intent(c, SendTextActivity.class);
                        i.putExtra(NODE_NUM_KEY, nodeNum);
                        startActivity(i);
                    }
                });

        ((Button) findViewById(R.id.sendCamButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent i = new Intent(c, PhotosActivity.class);
                        i.putExtra(NODE_NUM_KEY, nodeNum);
                        startActivity(i);
                    }
                });
    }

    // make sure that observer has always been added and removed - see Activity
    // lifecycle
    @Override
    protected void onResume()
    {
        super.onResume();
        nc.addNetworkObserver(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        nc.removeNetworkObserver(this);
    }

    private void updateInterfaceWithHeartbeat(final Heartbeat hb)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                // Set title text
                ((TextView) findViewById(R.id.nodeDetailsTitle))
                        .setText("Node " + displayedNode.nodeNum);

                if (hb != null)
                {
                    String batterySensorStr = hb.sensorOutput
                            .get(SensorType.Battery);

                    String batteryStr = batterySensorStr == null ? "No battery"
                            : new BatteryStatus(batterySensorStr).toString();

                    ((TextView) findViewById(R.id.battery)).setText("Battery: "
                            + batteryStr);

                    // Set compass reading
                    String compassSensorStr = hb.sensorOutput
                            .get(SensorType.Compass);
                    String compassStr = compassSensorStr == null ? "No compass"
                            : new CompassReading(compassSensorStr).toString();

                    ((TextView) findViewById(R.id.compass)).setText("Compass: "
                            + compassStr);

                    // Set GPS reading
                    String latStr;
                    String lonStr;
                    String locStr = hb.sensorOutput.get(SensorType.GPS);
                    if (locStr == null)
                        latStr = lonStr = "No Location";
                    else
                    {
                        Location loc = new Location(locStr);
                        latStr = loc.latitude + "";
                        lonStr = loc.longitude + "";
                    }

                    ((TextView) findViewById(R.id.lat)).setText("Latitude: "
                            + latStr);

                    ((TextView) findViewById(R.id.lon)).setText("Longitude: "
                            + lonStr);

                    // Set last heartbeat time
                    Date d = new Date(hb.timestamp);
                    String formattedDate = new SimpleDateFormat("H:mm:ss")
                            .format(d);
                    ((TextView) findViewById(R.id.lastHB))
                            .setText("Last heartbeat: " + formattedDate);
                }
            }
        });
    }

    // event received from the NetworkController
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        if (netEvent.event == Event.RecvdHeartbeat
                || netEvent.event == Event.SentHeartbeat)
        {
            Heartbeat hb = (Heartbeat) netEvent.data;
            if (hb.from == displayedNode.nodeNum)
                updateInterfaceWithHeartbeat(hb);
        }
    }
}
