package sdmay1207.ais.network;

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
    private Receiver r;
    private boolean isRunning = false;

    // A map of all nodes which have been seen so far - at this point, may or
    // may not include this node
    private Map<Integer, Node> knownNodes = new ConcurrentHashMap<Integer, Node>();

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand,
        RecvdTextMessage
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
     * Returns all known nodes. Depends on which nodes we've received a
     * heartbeat from, not which nodes the routing algorithm has detected
     * 
     * @return a list of all nodes in the entire mesh network that this node can
     *         currently connect to.
     */
    public Map<Integer, Node> getNodesInNetwork()
    {
        return knownNodes;
    }

    /**
     * @return a list of all nodes that this node is directly connected to
     */
    public Map<Integer, Node> neighborNodes()
    {
        Set<Integer> neighbors = networkInterface.routingImpl
                .getZeroHopNeighbors();
        Map<Integer, Node> neighborMap = new HashMap<Integer, Node>();

        for (Integer neighborAddr : neighbors)
        {
            Node neighborNode = knownNodes.get(neighborAddr);
            if (neighborNode == null)
                System.err.println("Problem: node " + neighborAddr
                        + " isn't known by the NetworkController");
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
    }

    // TODO not really sure how to structure this- events being passed from
    // wherever it's detected to where it needs to be handled
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

        public Receiver()
        {
            receivedEvents = new ConcurrentLinkedQueue<NetworkEvent>();
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

        public void addMessage(String fromIP, byte[] data)
        {
            NetworkMessage msg = NetworkMessage.getMessage(fromIP, data);
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
            }

            addEvent(event);
        }

        public void nodeLeft(int nodeNumber)
        {
            addEvent(new NetworkEvent(Event.NodeLeft, nodeNumber));
        }

        public void nodeJoined(int nodeNumber)
        {
            addEvent(new NetworkEvent(Event.NodeJoined, nodeNumber));
        }

        /**
         * Add the event to the queue, then wake the notifer thread
         */
        private void addEvent(NetworkEvent event)
        {
            receivedEvents.add(event);
            synchronized (receivedEvents)
            {
                receivedEvents.notify();
            }
        }
    }
}