package sdmay1207.ais.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private NetworkInterface networkInterface;
    private Receiver r;

    // A map of all nodes which have been seen so far
    private Map<Integer, Node> knownNodes = new ConcurrentHashMap<Integer, Node>();

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand
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
    public void start(int nodeNumber, RoutingAlg routingAlg)
    {
        r = new Receiver();
        new Thread(r).start();

        networkInterface = new NetworkInterface(r);
        networkInterface.startNetwork(nodeNumber);
        networkInterface.startRouting(routingAlg);
    }
    
    public void stop()
    {
        if (networkInterface != null)
            networkInterface.stop();
        
        if (r != null)
            r.stop();
    }

    public boolean sendHeartbeat(Heartbeat hb)
    {
        return networkInterface.broadcastData(hb);
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
    public class Receiver implements Runnable
    {
        private Queue<NetworkEvent> receivedEvents;

        // http://stackoverflow.com/questions/106591/do-you-ever-use-the-volatile-keyword-in-java
        private volatile boolean keepRunning = true;

        public Receiver()
        {
            receivedEvents = new ConcurrentLinkedQueue<NetworkEvent>();
        }

        public void run()
        {
            while (keepRunning)
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
        }
        
        public void stop()
        {
            keepRunning = false;
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
            }

            receivedEvents.add(event);
            synchronized (receivedEvents)
            {
                receivedEvents.notify();
            }
        }

        public void nodeLeft(int nodeNumber)
        {
            receivedEvents.add(new NetworkEvent(Event.NodeLeft, nodeNumber));
        }

        public void nodeJoined(int nodeNumber)
        {
            receivedEvents.add(new NetworkEvent(Event.NodeJoined, nodeNumber));
        }
    }
}