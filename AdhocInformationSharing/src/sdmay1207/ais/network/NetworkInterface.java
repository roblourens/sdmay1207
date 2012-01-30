package sdmay1207.ais.network;

import sdmay1207.ais.network.routing.AODV;
import sdmay1207.ais.network.routing.BATMAN;
import sdmay1207.ais.network.routing.RoutingImpl;

/**
 * The network interface component allows the system to connect to a network and
 * send and receive data.
 */
public class NetworkInterface
{
    public static final String SUBNET = "192.168.2.";

    public enum RoutingAlg
    {
        AODV, BATMAN
    }
    
    private RoutingImpl routingImpl = null;
    private int nodeNumber;

    /**
     * Starts the adhoc network
     * 
     * @param nodeNumber
     *            The last part of the IP address
     * @return True if successful
     */
    public boolean startNetwork(int nodeNumber)
    {
        this.nodeNumber = nodeNumber;
        return false;
    }

    /**
     * Starts the routing implementation
     * 
     * @param routingAlg
     *            The routing algorithm to use
     * @return True if successful
     */
    public boolean startRouting(RoutingAlg routingAlg)
    {
        switch (routingAlg)
        {
        case AODV:
            routingImpl = new AODV(SUBNET+nodeNumber);
            break;
        case BATMAN:
            routingImpl = new BATMAN(SUBNET+nodeNumber);
            break;
        }
        return false;
    }

    /**
     * Power the networking hardware off
     * 
     * @return True if successful
     */
    public boolean powerOff()
    {
        return false;
    }

    /**
     * Power the networking hardware on
     * 
     * @return True if successful
     */
    public boolean powerOn()
    {
        return false;
    }

    /**
     * Determine the wireless link quality, out of 70. The teens is the danger
     * zone
     * 
     * @return The wireless link quality
     */
    public int getLinkQuality()
    {
        return 0;
    }
    
    public boolean broadcastData(Object data)
    {
        return transmitData(255, data);
    }
    
    public boolean transmitData(int nodeNum, Object data)
    {
        // transmit it
        return routingImpl.transmitData(SUBNET+nodeNum, data.toString());
    }
}
