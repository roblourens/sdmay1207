package com.androidhive.dashboard;

import java.util.Random;

import sdmay1207.ais.NodeController;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;
import android.app.Application;
import android.util.Log;

public class DashboardApplication extends Application
{
    public NodeController nc = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Log.d("application", "app onCreate");

        Random r = new Random();
        int nodeNumber = r.nextInt(245) + 10; // reserve the single-digit
                                              // ones

        String dataDir = getApplicationContext().getFilesDir().getParent();
        nc = new NodeController(nodeNumber, dataDir);
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));
        
        System.out.println("Node #: " + nodeNumber);
    }
}
