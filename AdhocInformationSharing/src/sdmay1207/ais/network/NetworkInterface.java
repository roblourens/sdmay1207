package sdmay1207.ais.network;

import sdmay1207.ais.Config;
import sdmay1207.ais.Device;
import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.NetworkController.Receiver;
import sdmay1207.ais.network.routing.AODV;
import sdmay1207.ais.network.routing.BATMAN;
import sdmay1207.ais.network.routing.RoutingImpl;

/**
 * The network interface component allows the system to connect to a network and
 * send and receive data.
 */
public class NetworkInterface
{
    public enum RoutingAlg
    {
        AODV, BATMAN
    }

    RoutingImpl routingImpl = null;
    private int nodeNumber;
    private Receiver receiver;

    public NetworkInterface(Receiver receiver)
    {
        this.receiver = receiver;
    }

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

        if (Device.isAndroidSystem())
        {
            String command = "su -c \"/data/data/adhoc/start_adhoc " + getIP()
                    + "\"";
            System.out.println("Executing: " + command);
            System.out.println("result: " + Device.sysCommand(command));
        } else
        {
            String result = Device.sysCommand("stop network-manager");
            if (result.startsWith("stop: Rejected"))
                throw new RuntimeException("Superuser privileges required");

            Device.sysCommand("ifconfig " + Device.wlanInterfaceName()
                    + " down");
            Device.sysCommand("iwconfig " + Device.wlanInterfaceName()
                    + " mode ad-hoc essid " + Config.ESSID);
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName() + " up");

            // Let it start up...
            Utils.sleep(500);

            System.out
                    .println("Starting the adhoc network with IP: " + getIP());
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName() + " "
                    + getIP());
            Device.sysCommand("ifconfig " + Device.wlanInterfaceName()
                    + " subnet 255.255.255.0");

        }

        return true;
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
            routingImpl = new AODV(receiver);
            break;
        case BATMAN:
            routingImpl = new BATMAN(receiver, Device.getDataDir(),
                    Device.wlanInterfaceName());
            break;
        }

        return routingImpl.start(Config.SUBNET, nodeNumber);
    }

    public void stop()
    {
        if (routingImpl != null)
            routingImpl.stop();

        // bring the interface down
        Device.sysCommand("ifconfig " + Device.wlanInterfaceName() + " down");
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
        return Config.SUBNET + nodeNumber;
    }

    public boolean broadcastData(Object data)
    {
        System.out.println("Broadcasting: " + data.toString());
        return routingImpl.broadcastData(data.toString());
    }

    public boolean sendData(int nodeNum, Object data)
    {
        System.out.println("Transmitting to node " + nodeNum + ": "
                + data.toString());
        return routingImpl.transmitData(nodeNum, data.toString());
    }
}
