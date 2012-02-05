package sdmay1207.ais.network;

import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.NetworkMessage;

/**
 * The network controller component encapsulates all behavior related to
 * deciding how and when to connect to a network, and managing the way data is
 * exchanged between the application and the network.
 */
public class NetworkController extends Observable
{
    private NetworkInterface networkInterface;

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand
    }

    /**
     * Do setup
     */
    public NetworkController(int nodeNumber, RoutingAlg routingAlg)
    {
        Receiver r = new Receiver();
        new Thread(r).start();
        
        networkInterface = new NetworkInterface(r);
        networkInterface.startNetwork(nodeNumber);
        networkInterface.startRouting(routingAlg);
    }

    public boolean sendHeartbeat(Heartbeat hb)
    {
        return networkInterface.broadcastData(hb);
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

    // TODO not really sure how to structure this- events being passed from
    // whevever it's detected to where it needs to be handled
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
