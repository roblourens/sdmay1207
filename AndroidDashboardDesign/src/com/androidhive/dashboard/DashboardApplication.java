package com.androidhive.dashboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;

import com.sdmay1207.sensors.BatterySensor;
import com.sdmay1207.sensors.CompassSensor;
import com.sdmay1207.sensors.GPSSensor;

import sdmay1207.ais.Device;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.Point2PointCommander.GoToLocCommand;
import sdmay1207.cc.Point2PointCommander.P2PState;
import sdmay1207.cc.Point2PointCommander.Point2PointGUI;
import sdmay1207.networkrejoining.NetworkRejoinMonitor.NetworkRejoinListener;
import android.app.Application;
import android.graphics.Color;
import android.util.Log;
import androidhive.dashboard.R;

public class DashboardApplication extends Application
{
    public NodeController nc = null;
    public NotificationManager nm;
    public GPSSensor gps;
    public HashMap<Integer, String> text;
    public HashMap<Integer, Integer> lastChecked;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d("application", "app onCreate");

        Random r = new Random();
        int nodeNumber = r.nextInt(245) + 10; // reserve the single-digit
                                              // ones
        text = new HashMap<Integer, String>();
        lastChecked = new HashMap<Integer, Integer>();
        String filename = "Sidewalks.osm";
        String dataRoot = getApplicationContext().getFilesDir().getParent();
        File dataDir = new File(dataRoot, "/files");
        // if (!new File(dataDir, filename).exists())
        // {
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
        // }

        nc = new NodeController(nodeNumber, dataDir.toString());
        nc.addSensor(new BatterySensor(this));
        nc.addSensor(new CompassSensor(this));
        gps = new GPSSensor(this);
        nc.addSensor(gps);

        nm = new NotificationManager();
        nc.p2pCmdr.setGUI(nm);

        System.out.println("Node #: " + nodeNumber);
    }

    // must call gps start on UI thread first! (or after)
    public void start()
    {
        Device.doAndroidHardStop();
        nc.start(RoutingAlg.AODV);
    }

    public void stop()
    {
        nc.stop();
        gps.stop();
        Device.doAndroidHardStop();
    }

    public class NotificationManager extends Observable implements Observer,
            NetworkRejoinListener, Point2PointGUI
    {
        public List<Notification> notifications = new ArrayList<Notification>();
        public List<DrawableNotification> notificationsToDraw = new ArrayList<DrawableNotification>();

        private DrawableNotification curP2PNotification;

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
            // and left/joined events should be displayed
            // java sucks
            if (Arrays.asList(eventsToBeNotifiedAbout).contains(netEvent.event))
                addNotification(new NetworkEventNotification(netEvent));
        }

        public void addNotification(Notification n)
        {
            notifications.add(n);

            setChanged();
            notifyObservers(n);
        }

        public void addNotification(DrawableNotification dn)
        {
            notificationsToDraw.add(dn);
            addNotification((Notification) dn);
        }

        public void lostSingleNode()
        {
            // ignore
        }

        public void networkSplit(final Location p)
        {
            addNotification(new DrawableNotification(
                    "We lost several nodes at once - you should move to " + p
                            + " to rejoin them.")
            {
                @Override
                public OverlayItem getOverlayItem()
                {
                    OverlayItem oi = new OverlayItem("notification", "title",
                            "desc", new GeoPoint(p.latitude, p.longitude));
                    oi.setMarker(getResources()
                            .getDrawable(R.drawable.dualflag));
                    return oi;
                }
            });
        }

        public void lostEntireNetwork(final Location p)
        {
            addNotification(new DrawableNotification(
                    "The entire network has become disconnected - you should move to "
                            + p + " to rejoin it.")
            {
                @Override
                public OverlayItem getOverlayItem()
                {
                    OverlayItem oi = new OverlayItem("notification", "title",
                            "desc", new GeoPoint(p.latitude, p.longitude));
                    oi.setMarker(getResources()
                            .getDrawable(R.drawable.dualflag));
                    return oi;
                }
            });
        }

        public void p2pInitiated(final GoToLocCommand command)
        {
            DrawableNotification p2pNotification = new DrawableNotification(
                    "Node "
                            + command.from
                            + " has initiated a point-to-point task. Go to "
                            + command.loc
                            + " to relay video."
                            + (command.headNodeNum == nc.getMe().nodeNum ? " You are the head node."
                                    : "")
                            + (command.tailNodeNum == nc.getMe().nodeNum ? " You are the tail node."
                                    : ""))
            {
                private List<Location> pathToDest;

                @Override
                public OverlayItem getOverlayItem()
                {
                    OverlayItem oi = new OverlayItem("notification", "title",
                            "desc", new GeoPoint(command.loc.latitude,
                                    command.loc.longitude));
                    oi.setMarker(getResources().getDrawable(R.drawable.flag));
                    return oi;
                }

                @Override
                public void drawOverlay(OverlayManager om)
                {
                    PathOverlay po = new PathOverlay(Color.BLUE,
                            DashboardApplication.this);

                    Location here = nc.getMe().lastLocation;
                    if (pathToDest == null)
                        pathToDest = nc.p2pCmdr.getWrangler()
                                .getPathBetweenPoints(here, command.loc);

                    for (Location loc : pathToDest)
                        po.addPoint(new GeoPoint(loc.latitude, loc.longitude));

                    om.add(po);
                }
            };

            // working on one currently? if so, replace it
            if (curP2PNotification != null)
            {
                System.out
                        .println("Got a p2p notification to replace the current one SO YOU CAN GO AND TELL THAT");
                notificationsToDraw.remove(curP2PNotification);
            }
            curP2PNotification = p2pNotification;

            addNotification(p2pNotification);
        }

        public void stateChanged(P2PState newState)
        {
            System.out.println("Entered " + newState.name() + " state");
            switch (newState)
            {
            // enRoute is pretty much covered above
            case searching:
                addNotification(new Notification(
                        "You have reached your assigned area, now try to join up with neighbors"));
                break;
            case waiting:
                addNotification(new Notification(
                        "You've joined up with neighbors, now wait for the head and tail nodes to join"));
                break;
            case ready:
                addNotification(new Notification(
                        "Tail node is ready - start streaming whenever you want"));
                break;
            case enRouteToRallyPoint:
                addNotification(new Notification(
                        "Connecting to head and tail timed out - return to rally point"));
                break;
            case active:
                addNotification(new Notification("Now streaming"));
                break;
            case inactive:
                addNotification(new Notification(
                        "The point-to-point task has finished"));
                break;
            }
        }
    }

    /*
     * complicated - this whole system could almost be replaced with a list of
     * asdfasdf Strings but we might want to add some extra behavior for a
     * notification later - String of extra details, timestamps, locations,
     * overlay icons/colors linked to notification types, whatever, so I figure
     * set up a simple object for now
     */
    public class Notification
    {
        public long timestamp;
        private String displayStr = "";

        public Notification()
        {
            timestamp = System.currentTimeMillis();
        }

        public Notification(String displayStr)
        {
            this();
            this.displayStr = displayStr;
        }

        public String shortDisplayString()
        {
            return displayStr;
        }

        public boolean isTextMessage()
        {
            return (this instanceof NetworkEventNotification && ((NetworkEventNotification) this).netEvent.event == Event.RecvdTextMessage);
        }
    }

    public abstract class DrawableNotification extends Notification
    {
        public DrawableNotification(String displayStr)
        {
            super(displayStr);
        }

        public OverlayItem getOverlayItem()
        {
            return null;
        }

        public void drawOverlay(OverlayManager om)
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
