package sdmay1207.ais.network.routing;

// something useful for AODV and BATMAN to interface with this
public interface RoutingImpl
{
    public boolean transmitData(String ip, String data);

    public boolean broadcastData(String subnet, String data);

    public boolean start(String ip, String interfaceName);

    public boolean stop();
}
