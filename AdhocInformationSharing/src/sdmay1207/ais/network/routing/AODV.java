package sdmay1207.ais.network.routing;

import sdmay1207.ais.network.routing.RoutingImpl;

// interfaces with the AODV bachelor's implementation
// just calls methods in Node, etc. to send data
public class AODV implements RoutingImpl
{
    @Override
    public boolean transmitData(String ip, String data)
    {
        return false;
    }
    
    public boolean broadcastData(String ip, String data)
    {
        return false;
    }

    @Override
    public boolean start(String ip, String interfaceName)
    {
        return false;
    }

    @Override
    public boolean stop()
    {
        return false;
    }
}
