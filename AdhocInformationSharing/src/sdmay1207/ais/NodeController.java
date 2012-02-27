package sdmay1207.ais;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.NetworkController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.Sensor;
import sdmay1207.ais.sensors.SensorInterface;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

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
    // other components
    private NetworkController networkController;
    private SensorInterface sensorInterface = new SensorInterface();
    private Node me;
    private HeartbeatTask ht;

    // config
    private int nodeNumber;

    // static config
    private static final int HEARTBEAT_FREQ = 5000; // ms
    private static final String DEFAULT_DATA_DIR = "~/.sdmay1207";

    public NodeController(int nodeNumber, String dataDir)
    {
        Device.setDataDir(dataDir);
        this.nodeNumber = nodeNumber;

        me = new Node(nodeNumber);
        networkController = new NetworkController();
        networkController.addObserver(this);

        if (dataDir == null || dataDir.equals(""))
            dataDir = DEFAULT_DATA_DIR;
    }

    /**
     * Turn on the network, initialize routing, start sending hearbeats
     */
    public void start(RoutingAlg routingAlg)
    {
        networkController.start(nodeNumber, routingAlg);
        ht = new HeartbeatTask();
        new Thread(ht).start();
    }

    /**
     * SHUT. DOWN. EVERYTHING.
     */
    public void stop()
    {
        networkController.stop();

        if (ht != null)
            ht.stop();
    }

    /**
     * Adds a sensor to this node. e.g. implemented using Android classes or
     * whatever
     * 
     * @param s
     *            An instance of the Sensor object
     */
    public void addSensor(Sensor s)
    {
        sensorInterface.addSensor(s);
        me.addSensorType(s.getType());
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
            System.out.println("Got heartbeat from " + hb.from + ": " + hb.toString());
            // do something useful with it- pass to GUI or something
            // **or GUI has actually registered as the listener** this
            break;
        case NodeJoined:
            System.out.println("Node joined: " + netEvent.data);
            break;
        case NodeLeft:
            System.out.println("Node left: " + netEvent.data);
            break;
        case RecvdCommand:
            System.out.println("Received command: " + netEvent.data);
            break;
        case RecvdData:
            System.out.println("Received data: " + netEvent.data);
            break;
        }
    }

    public Map<Integer, Node> getNodesInNetwork()
    {
        return networkController.getNodesInNetwork();
    }

    public class HeartbeatTask implements Runnable
    {
        private volatile boolean keepRunning = true;

        @Override
        public void run()
        {
            while (keepRunning)
            {
                System.out.println("...ba-dump...");
                Heartbeat hb = me.getHeartbeat();

                // Add sensor output to hearbeat message
                for (SensorType sType : me.sensors)
                {
                    Object reading = sensorInterface.sensors.get(sType)
                            .getReading();
                    if (reading != null)
                        hb.sensorOutput.put(sType, reading.toString());
                }

                networkController.sendHeartbeat(hb);
                Utils.sleep(HEARTBEAT_FREQ);
            }
        }

        public void stop()
        {
            keepRunning = false;
        }
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

        new NodeController(nodeNumber, null).start(routingAlg);
    }
}