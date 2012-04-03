package sdmay1207.ais.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import sdmay1207.ais.etc.Repeater;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.network.model.Node;

/**
 * The network controller component encapsulates all behavior related to
 * deciding how and when to connect to a network, and managing the way data is
 * exchanged between the application and the network.
 */
public class NetworkController extends Observable
{
    public NetworkInterface networkInterface;
    public Receiver r;
    private boolean isRunning = false;

    // Note: node objects between knownNodes/connectedNodes are the same
    // A map of all nodes which have been seen so far - at this point, may or
    // may not include this node
    private Map<Integer, Node> knownNodes = new ConcurrentHashMap<Integer, Node>();

    // only nodes which are currently connected
    private Map<Integer, Node> connectedNodes = new ConcurrentHashMap<Integer, Node>();

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand,
        RecvdTextMessage, RecvdShuttingDownMessage, SentHeartbeat
    }

    /**
     * A message object returned to observers on each network event.
     */
    public static class NetworkEvent
    {
        public Event event;

        public Object data;

        public long rcvdTimestamp;

        public NetworkEvent(Event event, Object data)
        {
            this.event = event;
            this.data = data;
            this.rcvdTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Turn on the adhoc network, start the routing system
     */
    public boolean start(int nodeNumber, RoutingAlg routingAlg)
    {
        r = new Receiver();
        r.start();

        networkInterface = new NetworkInterface(r);

        isRunning = networkInterface.startNetwork(nodeNumber)
                && networkInterface.startRouting(routingAlg);

        return isRunning;
    }

    /**
     * Shut off the network, stop the routing system
     */
    public void stop()
    {
        if (networkInterface != null)
            networkInterface.stop();

        if (r != null)
            r.stop();

        isRunning = false;
    }

    /**
     * Broadcasts the heartbeat object
     */
    public boolean sendHeartbeat(Heartbeat hb)
    {
        return networkInterface.broadcastData(hb);
    }

    /**
     * Sends the command to the specified node
     */
    public boolean sendNetworkMessage(NetworkMessage message, int destNodeNum)
    {
        return networkInterface.sendData(destNodeNum, message);
    }

    public boolean broadcastNetworkMessage(NetworkMessage message)
    {
        return networkInterface.broadcastData(message);
    }

    /**
     * Returns all connected nodes. Depends on which nodes we've received a
     * heartbeat from, not which nodes the routing algorithm has detected
     * 
     * @return a list of all nodes in the entire mesh network that this node can
     *         currently connect to.
     */
    public Map<Integer, Node> getNodesInNetwork()
    {
        return connectedNodes;
    }

    /**
     * Returns all nodes which have ever been seen
     */
    public Map<Integer, Node> getKnownNodes()
    {
        return knownNodes;
    }

    /**
     * @return a list of all nodes that this node is directly connected to
     */
    public Map<Integer, Node> neighborNodes()
    {
        // not running? return an empty map
        if (!isRunning)
            return new HashMap<Integer, Node>();

        Set<Integer> neighbors = networkInterface.routingImpl
                .getZeroHopNeighbors();
        Map<Integer, Node> neighborMap = new HashMap<Integer, Node>();

        for (Integer neighborAddr : neighbors)
        {
            Node neighborNode = connectedNodes.get(neighborAddr);
            if (neighborNode == null)
                System.err
                        .println("Problem: node "
                                + neighborAddr
                                + " isn't connected according to the NetworkController");
            else
                neighborMap.put(neighborAddr, neighborNode);
        }

        return neighborMap;
    }

    public void transmitData(int nodeNum, byte[] data)
    {
        networkInterface.sendData(nodeNum, data);
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Update the map of known nodes with the info from the given Heartbeat
     * object
     */
    private void updateKnownNodes(Heartbeat hb)
    {
        if (knownNodes.get(hb.from) == null)
            knownNodes.put(hb.from, new Node(hb.from));

        knownNodes.get(hb.from).update(hb);

        if (!connectedNodes.containsKey(hb.from))
            connectedNodes.put(hb.from, knownNodes.get(hb.from));
    }

    /**
     * Receives data/events from the network, interprets it into the correct
     * message
     * 
     * @author rob
     * 
     */
    public class Receiver extends Repeater
    {
        private Queue<NetworkEvent> receivedEvents;
        private DatagramSocket localSock;

        private final int CAM_STREAM_PORT = 7476;
        private final InetAddress CAM_STREAM_ADDR;

        public Receiver()
        {
            receivedEvents = new ConcurrentLinkedQueue<NetworkEvent>();
            try
            {
                CAM_STREAM_ADDR = InetAddress.getByName("127.0.0.1");
                localSock = new DatagramSocket();
            } catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(
                        "Could not set up camera stream socket");
            }
        }

        @Override
        protected void runOnce()
        {
            synchronized (receivedEvents)
            {
                while (receivedEvents.isEmpty())
                {
                    try
                    {
                        receivedEvents.wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            NetworkEvent event = receivedEvents.poll();
            setChanged();
            notifyObservers(event);
        }

        public void addMessageData(String fromIP, byte[] data)
        {
            // is camera data?
            if (data[0] == -128 && (data[1] == 74 || data[1] == -54))
            {
                handleCameraStreamPacket(data);
                return;
            }

            NetworkMessage msg = NetworkMessage.getMessage(fromIP, data);
            addMessage(msg);
        }

        public void addMessage(NetworkMessage msg)
        {
            NetworkEvent event = null;

            switch (msg.messageType)
            {
            case Command:
                event = new NetworkEvent(Event.RecvdCommand, msg);
                break;
            case Heartbeat:
                event = new NetworkEvent(Event.RecvdHeartbeat, msg);
                updateKnownNodes((Heartbeat) msg);
                break;
            case TextMessage:
                event = new NetworkEvent(Event.RecvdTextMessage, msg);
                break;
            case ShuttingDown:
                event = new NetworkEvent(Event.RecvdShuttingDownMessage, msg);
                break;
            }

            addEvent(event);
        }

        public void nodeLeft(int nodeNumber)
        {
            connectedNodes.remove(nodeNumber);
            addEvent(new NetworkEvent(Event.NodeLeft, nodeNumber));
        }

        // Let's only add nodes to connected when we get a heartbeat
        public void nodeJoined(int nodeNumber)
        {
            addEvent(new NetworkEvent(Event.NodeJoined, nodeNumber));
        }

        /**
         * Add the event to the queue, then wake the notifer thread
         */
        public void addEvent(NetworkEvent event)
        {
            receivedEvents.add(event);
            synchronized (receivedEvents)
            {
                receivedEvents.notify();
            }
        }

        public void handleCameraStreamPacket(byte[] data)
        {
            try
            {
                System.out.println("Sending camera packet to "
                        + CAM_STREAM_ADDR + ", port " + CAM_STREAM_PORT);
                localSock.send(new DatagramPacket(data, data.length,
                        CAM_STREAM_ADDR, CAM_STREAM_PORT));
            } catch (IOException e)
            {
                e.printStackTrace();
                System.err.println("Problem sending camera data");
            }
        }
    }
}