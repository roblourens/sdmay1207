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
    private NetworkInterface networkInterface = new NetworkInterface(
            new Receiver());

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand
    }

    /**
     * Do setup
     */
    public NetworkController(int nodeNumber, RoutingAlg routingAlg)
    {
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

        public NetworkEvent(Event event, Object data)
        {
            this.event = event;
            this.data = data;
        }
    }

    /**
     * Receives data from the network, interprets it into the correct message
     * 
     * @author rob
     * 
     */
    public class Receiver implements Runnable
    {
        private Queue<NetworkMessage> receivedMessages;

        // http://stackoverflow.com/questions/106591/do-you-ever-use-the-volatile-keyword-in-java
        private volatile boolean keepRunning = true;

        public Receiver()
        {
            receivedMessages = new ConcurrentLinkedQueue<NetworkMessage>();
        }

        public void run()
        {
            while (keepRunning)
            {
                synchronized (receivedMessages)
                {
                    while (receivedMessages.isEmpty())
                    {
                        try
                        {
                            receivedMessages.wait();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                NetworkMessage msg = receivedMessages.poll();
                switch (msg.messageType)
                {
                case Command:
                    notifyObservers(new NetworkEvent(Event.RecvdCommand, msg));
                    break;
                case Heartbeat:
                    notifyObservers(new NetworkEvent(Event.RecvdHeartbeat, msg));
                    break;
                }
            }
        }

        public void addMessage(String fromIP, byte[] data)
        {
            receivedMessages.add(NetworkMessage.getMessage(fromIP, data));
            synchronized (receivedMessages)
            {
                receivedMessages.notify();
            }

        }
    }
}
