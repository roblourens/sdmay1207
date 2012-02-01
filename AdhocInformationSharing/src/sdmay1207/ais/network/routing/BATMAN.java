package sdmay1207.ais.network.routing;

import sdmay1207.ais.Device;

// wraps a socket to send data using BATMAN
public class BATMAN implements RoutingImpl
{
    @Override
    public boolean transmitData(String ip, String data)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean start(String ip, String interfaceName)
    {
        Device.sysCommand("su -C batmand " + interfaceName);
        return true;
    }

    @Override
    public boolean stop()
    {
        return false;
    }
}
