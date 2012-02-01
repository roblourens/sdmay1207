package sdmay1207.ais.network;

import sdmay1207.ais.Device;
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
    public static final String ESSID = "sdmay1207";

    public enum RoutingAlg
    {
        AODV, BATMAN
    }

    private RoutingImpl routingImpl = null;
    private int nodeNumber;

    // Need a way to detect whether this was started w/o sudo
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

        if (!Device.isAndroidSystem())
        {
            String result = Device.sysCommand("stop network-manager");
            if (result.startsWith("stop: Rejected"))
                throw new RuntimeException("Superuser privileges required");
            
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName()
                    + " down");
            Device.sysCommand("iwconfig " + Device.wlanInterfaceName()
                    + " mode ad-hoc essid " + ESSID);
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName() + " up");

            // Let it start up...
            try
            {
                Thread.sleep(1);
            } catch (InterruptedException e)
            {
            }

            System.out
                    .println("Starting the adhoc network with IP: " + getIP());
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName() + " "
                    + getIP());
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName()
                    + " subnet 255.255.255.0");
        }

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
            routingImpl = new AODV();
            break;
        case BATMAN:
            routingImpl = new BATMAN();
            break;
        }

        return routingImpl.start(getIP(), Device.wlanInterfaceName());
    }

    // any reason to implement these two? It was in the design doc but I'm not
    // sure why we would need it
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

    private String getIP()
    {
        return SUBNET + nodeNumber;
    }

    public boolean broadcastData(Object data)
    {
        return transmitData(255, data);
    }

    public boolean transmitData(int nodeNum, Object data)
    {
        // transmit it
        return routingImpl.transmitData(getIP(), data.toString());
    }

}
