package sdmay1207.android;

import java.awt.Button;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.text.View;

import sdmay1207.ais.Device;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.Compass.CompassReading;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;

public class Sdmay1207Activity extends Activity implements Observer
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
        
        nc.addNetworkObserver(this);
        
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
        
        Node n = nc.getNodesInNetwork().get(3);
        CompassReading cr = new CompassReading(n.lastHeartbeat.sensorOutput.get(SensorType.Compass));
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