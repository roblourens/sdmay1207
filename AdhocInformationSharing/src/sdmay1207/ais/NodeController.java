package sdmay1207.ais;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import sdmay1207.ais.network.NetworkController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.SensorInterface;

/**
 * The node controller component implements node logic by controlling the other
 * components. It will decide when to transmit sensor data to other nodes in the
 * network and what to do with data received from other nodes in the network. It
 * will receive commands from the GUI and decide how to complete them.
 * 
 * The node controller will also provide the ability for other applications to
 * work with the network. It will provide an interface which allows for
 * retrieving information about the network, sending arbitrary data, such as
 * commands, to other nodes, and other functionality useful for external
 * applications.
 */
public class NodeController implements Observer
{
    private NetworkController networkController;
    private SensorInterface sensorInterface = new SensorInterface();
    private Node me;

    // ms
    private static final int HEARTBEAT_FREQ = 5000;
    private static final String DEFAULT_DATA_DIR = "~/.sdmay1207";

    public NodeController(int nodeNumber, RoutingAlg routingAlg, String dataDir)
    {
        Device.setDataDir(dataDir);

        me = new Node(nodeNumber);
        networkController = new NetworkController(nodeNumber, routingAlg);
        networkController.addObserver(this);

        if (dataDir == null || dataDir.equals(""))
            dataDir = DEFAULT_DATA_DIR;
    }

    public void start()
    {
        new Timer().schedule(new HeartbeatTask(), 0);
    }

    // probably won't usually be used - GUI should call the constructor instead
    public static void main(String[] args)
    {
        if (args.length < 2)
            throw new RuntimeException(
                    "Must provide the node number and routing protocol (A (AODV) or B (BATMAN))");

        int nodeNumber = 0;
        try
        {
            nodeNumber = Integer.parseInt(args[0]);
            if (nodeNumber < 1 || nodeNumber > 254)
                throw new RuntimeException("Node number " + nodeNumber
                        + " out of bounds");
        } catch (NumberFormatException e)
        {
            throw new RuntimeException("Go to hell");
        }

        RoutingAlg routingAlg;
        if (args[1].equals("A"))
            routingAlg = RoutingAlg.AODV;
        else if (args[1].equals("B"))
            routingAlg = RoutingAlg.BATMAN;
        else
            throw new RuntimeException(args[1]
                    + " isn't a routing algorithm! Enter A or B");

        new NodeController(nodeNumber, routingAlg, null).start();
    }

    // event received from the NetworkController
    @Override
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event)
        {
        case RecvdHeartbeat:
            Heartbeat hb = (Heartbeat) netEvent.data;
            System.out.println("Got heartbeat from " + hb.from);
            // do something useful with it- pass to GUI or something
            // or GUI has actually registered as the listener
            break;
        }
    }

    public class HeartbeatTask extends TimerTask
    {
        @Override
        public void run()
        {
            System.out.println("...ba-dump...");
            networkController.sendHeartbeat(me.getHeartbeat());

            new Timer().schedule(new HeartbeatTask(), HEARTBEAT_FREQ);
        }
    }
}