package sdmay1207.ais.network;

import java.util.Observable;

import sdmay1207.ais.sensors.GPS.GPSReading;

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
    public NetworkController()
    {
        networkInterface = new NetworkInterface();
    }

    /**
     * A message object returned to observers on each network event.
     */
    public class NetworkEvent
    {
        public Event event;

        public Object data;
    }

    public class Heartbeat
    {
        public Node from;

        // Could be json here
        public String sensorOutput;

        public GPSReading location;
    }
}
