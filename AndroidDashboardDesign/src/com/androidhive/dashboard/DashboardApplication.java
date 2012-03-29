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

import org.osmdroid.views.overlay.OverlayManager;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.android.sensors.BatterySensor;
import sdmay1207.android.sensors.CompassSensor;
import sdmay1207.android.sensors.GPSSensor;
import sdmay1207.cc.Point2PointCommander.GoToLocCommand;
import sdmay1207.cc.Point2PointCommander.P2PState;
import sdmay1207.cc.Point2PointCommander.Point2PointGUI;
import sdmay1207.networkrejoining.NetworkRejoinMonitor.NetworkRejoinListener;
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
        String dataRoot = getApplicationContext().getFilesDir().getParent();
        File dataDir = new File(dataRoot, "/files");
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

        nc = new NodeController(nodeNumber, dataDir.toString());
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        nc.addSensor(new GPSSensor(this));

        nm = new NotificationManager();

        System.out.println("Node #: " + nodeNumber);
    }

    public class NotificationManager extends Observable implements Observer,
            NetworkRejoinListener, Point2PointGUI
    {
        public List<Notification> notifications = new ArrayList<Notification>();

        private Event[] eventsToBeNotifiedAbout = new Event[] {
                Event.NodeJoined, Event.NodeLeft,
                Event.RecvdShuttingDownMessage, Event.RecvdTextMessage };

        public NotificationManager()
        {
            nc.addNetworkObserver(this);
        }

        // Listen for events from the networkController
        public void update(Observable observable, Object obj)
        {
            NetworkEvent netEvent = (NetworkEvent) obj;

            // Everything that is a message (text message, all commands, etc.)
            // and
            // left/joined events should be displayed
            // Once we implement NetworkRejoinListener, maybe we can ditch these
            // joined/left events
            if (Arrays.asList(eventsToBeNotifiedAbout).contains(netEvent.event))
                addNotification(new NetworkEventNotification(netEvent));
        }

        public void addNotification(Notification n)
        {
            notifications.add(0, n);

            setChanged();
            notifyObservers(n);
        }

        public void lostSingleNode()
        {
            // ignore
        }

        public void networkSplit(final Location p)
        {
            addNotification(new Notification()
            {
                @Override
                public String shortDisplayString()
                {
                    return "We lost several nodes at once - you should move to "
                            + p + " to rejoin them.";
                }
            });
        }

        public void lostEntireNetwork(final Location p)
        {
            addNotification(new Notification()
            {
                @Override
                public String shortDisplayString()
                {
                    return "The entire network has become disconnected - you should move to "
                            + p + " to rejoin it.";
                }
            });
        }

        public void p2pInitiated(final GoToLocCommand command)
        {
            addNotification(new Notification()
            {
                @Override
                public String shortDisplayString()
                {
                    return "Node " + command.from
                            + " has initiated a point-to-point task. Go to "
                            + command.loc + " to relay video.";
                }
                
                @Override
                public void addOverlay(OverlayManager om)
                {
                    super.addOverlay(om);
                    // add point and path here to command.loc
                }
            });
        }

        public void stateChanged(P2PState newState)
        {
            System.out.println("Entered " + newState.name() + " state");
            switch (newState)
            {
            // enRoute is pretty much covered above
            case searching:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "You have reached your assigned area, now try to join up with neighbors";
                    }
                });
                break;
            case waiting:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "You've joined up with neighbors, now wait for the head and tail nodes to join";
                    }
                });
                break;
            case ready:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "Tail node is ready - start streaming whenever you want";
                    }
                });
                break;
            case enRouteToRallyPoint:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "Connecting to head and tail timed out - return to rally point";
                    }
                });
                break;
            case active:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "Now streaming";
                    }
                });
                break;
            case inactive:
                addNotification(new Notification()
                {
                    @Override
                    public String shortDisplayString()
                    {
                        return "The point-to-point task has finished";
                    }
                });
                break;
            }
        }
    }

    /*
     * complicated - this whole system could almost be replaced with a list of asdfasdf
     * Strings but we might want to add some extra behavior for a notification
     * later - String of extra details, timestamps, locations, overlay
     * icons/colors linked to notification types, whatever, so I figure set up a
     * simple object for now
     */
    public abstract class Notification
    {
        public abstract String shortDisplayString();

        public void addOverlay(OverlayManager om)
        {

        }
    }

    public class NetworkEventNotification extends Notification
    {
        public NetworkEvent netEvent;

        public NetworkEventNotification(NetworkEvent netEvent)
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
