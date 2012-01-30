package sdmay1207.ais.network;

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

    /**
     * Starts the adhoc network
     * 
     * @param nodeNumber
     *            The last part of the IP address
     * @return True if successful
     */
    public boolean startNetwork(String nodeNumber)
    {
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
}
