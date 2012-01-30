package sdmay1207.ais.network.routing;

import sdmay1207.ais.network.routing.RoutingImpl;

// interfaces with the AODV bachelor's implementation
// just calls methods in Node, etc. to send data
public class AODV implements RoutingImpl
{
    public AODV(String ip)
    {
        // setup
    }

    @Override
    public boolean transmitData(String ip, String data)
    {
        return false;
    }
}
