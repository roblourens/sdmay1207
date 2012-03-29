package com.androidhive.dashboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;
import android.app.Application;
import android.util.Log;

public class DashboardApplication extends Application
{
    public NodeController nc = null;
    public NotificationManager nm;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d("application", "app onCreate");

        Random r = new Random();
        int nodeNumber = r.nextInt(245) + 10; // reserve the single-digit
                                              // ones

        String filename = "ISU_map.osm";
        String dataDir = getApplicationContext().getFilesDir().getParent();
        if (!new File(dataDir, filename).exists())
        {
            try
            {
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
        }

        nc = new NodeController(nodeNumber,
                new File(dataDir, "/files").toString());
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));

        nm = new NotificationManager();

        System.out.println("Node #: " + nodeNumber);
    }

    public class NotificationManager extends Observable implements Observer
    {
        public List<Notification> notifications = new ArrayList<Notification>();

        private Event[] eventsToBeNotifiedAbout = new Event[] {
                Event.NodeJoined, Event.NodeLeft, Event.RecvdCommand,
                Event.RecvdShuttingDownMessage, Event.RecvdTextMessage };

        public NotificationManager()
        {
            nc.addNetworkObserver(this);
        }

        public void update(Observable observable, Object obj)
        {
            NetworkEvent netEvent = (NetworkEvent) obj;

            // Everything that is a message (text message, all commands, etc.)
            // and
            // left/joined events should be displayed
            // Once we implement NetworkRejoinListener, maybe we can ditch these
            // joined/left events
            if (Arrays.asList(eventsToBeNotifiedAbout).contains(netEvent.event))
            {
                Notification n = new Notification(netEvent);
                notifications.add(0, n);

                setChanged();
                notifyObservers(n);
            }
        }
    }

    public class Notification
    {
        public NetworkEvent netEvent;

        public Notification(NetworkEvent netEvent)
        {
            this.netEvent = netEvent;
        }

        public String shortDisplayString()
        {
            if (netEvent.data instanceof NetworkMessage)
                return ((NetworkMessage) netEvent.data).description();

            switch (netEvent.event)
            {
            case NodeJoined:
                return "Node " + netEvent.data + " joined the network";
            case NodeLeft:
                return "Node " + netEvent.data + " left the network";
            }

            return "wat";
        }
    }
}
