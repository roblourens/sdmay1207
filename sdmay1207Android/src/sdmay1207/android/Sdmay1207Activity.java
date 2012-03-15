package sdmay1207.android;

import java.util.Random;

import sdmay1207.ais.Device;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Sdmay1207Activity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Random r = new Random();
        final int nodeNumber = r.nextInt(245) + 10; // reserve the single-digit
                                                    // ones

        String dataDir = getApplicationContext().getFilesDir().getParent();
        System.out.println("Using dataDir: " + dataDir);
        final NodeController nc = new NodeController(nodeNumber, dataDir);
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));
        
        ((Button) findViewById(R.id.startButton))
                .setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Device.sysCommand("su -c \"/data/data/android.tether/bin/tether stop 1\"");
                        System.out.println("Starting as node: " + nodeNumber);
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
    }
}