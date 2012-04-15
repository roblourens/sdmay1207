package sdmay1207.ais;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.etc.Repeater.TimedRepeater;
import sdmay1207.ais.network.NetworkController;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.NetworkCommand;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.network.model.NetworkMessage.MessageType;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.NetbookGPS;
import sdmay1207.ais.sensors.Sensor;
import sdmay1207.ais.sensors.SensorInterface;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import sdmay1207.cc.Point2PointCommander;
import sdmay1207.networkrejoining.NetworkRejoinMonitor;

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
    // For listeners
    private Map<String, List<CommandHandler>> commandHandlers = new HashMap<String, List<CommandHandler>>();

    // other components
    public NetworkController networkController;
    public SensorInterface sensorInterface = new SensorInterface();
    private Node me;
    private HeartbeatTask ht;
    public NetworkRejoinMonitor networkRejoinMonitor;
    public Point2PointCommander p2pCmdr;

    private boolean isRunning = false;

    // config
    private int nodeNumber;

    // static config
    private static final long HEARTBEAT_FREQ = 5000; // ms
    private static final String DEFAULT_DATA_DIR = "/usr/sdmay1207";

    public NodeController(int nodeNumber, String dataDir)
    {
        this.nodeNumber = nodeNumber;
        NetworkMessage.nodeNumMe = nodeNumber;

        if (dataDir == null || dataDir.equals(""))
            dataDir = DEFAULT_DATA_DIR;

        System.out.println("Starting with data directory " + dataDir);
        Device.setDataDir(dataDir);

        me = new Node(nodeNumber);
        networkController = new NetworkController();
        networkController.addObserver(this);

        p2pCmdr = new Point2PointCommander(this);
        networkRejoinMonitor = new NetworkRejoinMonitor(this);
    }

    /**
     * Turn on the network, initialize routing, start sending hearbeats
     */
    public void start(RoutingAlg routingAlg)
    {
        if (isRunning)
            return;

        // ensure that app is root on pc
        if (!Device.isAndroidSystem())
            if (!Device.sysCommand("whoami").trim().equals("root"))
                throw new RuntimeException("Must be run as root!");

        if (!networkController.start(nodeNumber, routingAlg))
            throw new RuntimeException("Network/routing did not start properly");

        ht = new HeartbeatTask(HEARTBEAT_FREQ);
        ht.start();

        networkRejoinMonitor.start();

        isRunning = true;
    }

    /**
     * SHUT. DOWN. EVERYTHING.
     */
    public void stop()
    {
        networkController.stop();

        if (ht != null)
            ht.stop();

        if (networkRejoinMonitor != null)
            networkRejoinMonitor.stop();

        isRunning = false;
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

    /**
     * Send a message to a node on the network
     */
    public void sendNetworkMessage(NetworkMessage message, int destNodeNum)
    {
        if (destNodeNum == me.nodeNum)
            networkController.r.addMessage(message);
        else
            networkController.sendNetworkMessage(message, destNodeNum);
    }

    public void broadcastNetworkMessage(NetworkMessage message)
    {
        networkController.broadcastNetworkMessage(message);
    }

    /**
     * Called when the node is shutting down and doesn't want to be found by the
     * network (battery dead, taking ball and going home, etc.)
     */
    public void broadcastShutdownAlert()
    {
        NetworkMessage msg = new NetworkMessage();
        msg.messageType = MessageType.ShuttingDown;
        networkController.broadcastNetworkMessage(msg);
    }

    /**
     * Register to be notified when a command type is received
     */
    public void registerForCommand(String commandType,
            CommandHandler commandHandler)
    {
        if (!commandHandlers.containsKey(commandType))
            commandHandlers.put(commandType, new ArrayList<CommandHandler>());

        commandHandlers.get(commandType).add(commandHandler);
    }

    public Object getSensorReading(SensorType sensorType)
    {
        return sensorInterface.getReading(sensorType);
    }

    /**
     * Returns a map of ALL known nodes (including this)
     */
    public Map<Integer, Node> getNodesInNetwork()
    {
        Map<Integer, Node> nodes = networkController.getNodesInNetwork();

        // modifies the map in NetworkController, deal with it
        nodes.put(nodeNumber, me);

        return nodes;
    }

    /**
     * Returns a map of ALL known nodes (including this)
     */
    public Map<Integer, Node> getKnownNodes()
    {
        Map<Integer, Node> nodes = networkController.getKnownNodes();

        // modifies the map in NetworkController, deal with it
        nodes.put(nodeNumber, me);

        return nodes;
    }
    
    /**
     * Returns a map of nodes which are direct neighbors of this node
     */
    public Map<Integer, Node> getNeighborNodes()
    {
        return networkController.neighborNodes();
    }

    /**
     * Access to the current node object
     */
    public Node getMe()
    {
        return me;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public interface CommandHandler
    {
        public void commandReceived(NetworkCommand command);
    }

    private class HeartbeatTask extends TimedRepeater
    {
        public HeartbeatTask(long freq)
        {
            super(freq);
        }

        protected void runOnce()
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
            hb.taskState = p2pCmdr.curState;
            networkController.r.addEvent(new NetworkEvent(Event.SentHeartbeat,
                    hb));

            networkController.sendHeartbeat(hb);
            me.update(hb);
        }
    }

    // see example implementation of update below
    public void addNetworkObserver(Observer obs)
    {
        networkController.addObserver(obs);
    }

    public void removeNetworkObserver(Observer obs)
    {
        networkController.deleteObserver(obs);
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
            System.out.println("Got heartbeat from " + hb.from + ": "
                    + hb.toString());
            break;
        case NodeJoined:
            System.out.println("Node joined: " + netEvent.data);
            break;
        case NodeLeft:
            System.out.println("Node left: " + netEvent.data);
            break;
        case RecvdCommand:
            NetworkCommand command = (NetworkCommand) netEvent.data;
            List<CommandHandler> typeHandlers = commandHandlers
                    .get(command.commandType);

            // Alert them
            for (CommandHandler handler : typeHandlers)
                handler.commandReceived(command);

            System.out.println("Received command: " + netEvent.data);
            break;
        case RecvdData:
            System.out.println("Received data: " + netEvent.data);
            break;
        case RecvdTextMessage:
            System.out.println("Received text message: " + netEvent.data);
            break;
        case RecvdShuttingDownMessage:
            System.out
                    .println("Received shutting down alert: " + netEvent.data);
            break;
        case SentHeartbeat:
            System.out.println("Sent hearbeat: " + netEvent.data);
        }
    }

    // probably won't usually be used - GUI should call the constructor instead
    // netbook testing only
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

        NodeController nc = new NodeController(nodeNumber, null);
        nc.addSensor(new NetbookGPS());
        nc.start(routingAlg);
    }
}