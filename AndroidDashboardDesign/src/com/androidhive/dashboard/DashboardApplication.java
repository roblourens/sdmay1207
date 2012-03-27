package com.androidhive.dashboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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

        try
        {
            String filename = "ISU_map.osm";
            System.out.println("Copying file '" + filename + "' ...");
            InputStream is = getAssets().open(filename);
            byte buf[] = new byte[1024];
            int len;
            OutputStream out = openFileOutput(filename, 0);
            while ((len = is.read(buf)) > 0)
                out.write(buf, 0, len);

            out.close();
            is.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Couldn't copy a file to the data dir!");
        }

        String dataDir = getApplicationContext().getFilesDir().getParent();
        nc = new NodeController(nodeNumber,
                new File(dataDir, "/files").toString());
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));

        System.out.println("Node #: " + nodeNumber);
    }
}
