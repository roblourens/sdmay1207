package sdmay1207.ais.network;

import java.util.Observable;

import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;

/**
 * The network controller component encapsulates all behavior related to
 * deciding how and when to connect to a network, and managing the way data is
 * exchanged between the application and the network.
 */
public class NetworkController extends Observable
{
    private NetworkInterface networkInterface = new NetworkInterface();

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
    }
}
