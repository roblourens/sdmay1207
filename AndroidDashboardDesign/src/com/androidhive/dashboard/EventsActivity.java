package com.androidhive.dashboard;

import sdmay1207.ais.Device;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import androidhive.dashboard.R;

public class EventsActivity extends Activity
{
    private NodeController nc;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_layout);

        nc = ((DashboardApplication) getApplication()).nc;

        ((Button) findViewById(R.id.startButton))
                .setOnClickListener(new OnClickListener()
                {
                    // @Override
                    public void onClick(View v)
                    {
                        Device.doAndroidHardStop();
                        nc.start(RoutingAlg.AODV);
                    }
                });

        ((Button) findViewById(R.id.endButton))
                .setOnClickListener(new OnClickListener()
                {
                    // @Override
                    public void onClick(View v)
                    {
                        nc.stop();
                        Device.doAndroidHardStop();
                    }
                });
    }
}
