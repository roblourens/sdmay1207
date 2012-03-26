package com.androidhive.dashboard;

import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.Battery.BatteryStatus;
import sdmay1207.ais.sensors.Compass.CompassReading;
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
        nc.addNetworkObserver(this);

        final int nodeNum = getIntent().getIntExtra(NODE_NUM_KEY, 0);
        displayedNode = nc.getKnownNodes().get(nodeNum);

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
    }

    private void updateInterfaceWithHeartbeat(Heartbeat hb)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                // Set title text
                ((TextView) findViewById(R.id.nodeDetailsTitle))
                        .setText("Node " + displayedNode.nodeNum);

                // Set battery level
                String batterySensorStr = displayedNode.lastHeartbeat.sensorOutput
                        .get(SensorType.Battery);
                String batteryStr = batterySensorStr == null ? "No battery"
                        : new BatteryStatus(batterySensorStr).toString();

                ((TextView) findViewById(R.id.battery)).setText("Battery: "
                        + batteryStr);

                // Set compass reading
                String compassSensorStr = displayedNode.lastHeartbeat.sensorOutput
                        .get(SensorType.Compass);
                String compassStr = compassSensorStr == null ? "No compass"
                        : new CompassReading(compassSensorStr).toString();
                
                ((TextView) findViewById(R.id.compass)).setText("Compass: "
                        + compassStr);
            }
        });
    }

    // event received from the NetworkController
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event)
        {
        case RecvdHeartbeat:
            Heartbeat hb = (Heartbeat) netEvent.data;
            if (hb.from == displayedNode.nodeNum)
                updateInterfaceWithHeartbeat(hb);
            break;
        }
    }
}
