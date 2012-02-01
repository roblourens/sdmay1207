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
        String result = Device.sysCommand("sudo batmand " + interfaceName);
        if (result.startsWith("Not using"))
        {
            System.out.println("batmand failed to start: " + result);
            return false;
        } else if (result.startsWith("Using "))
        {
            System.out.println("batmand started successfully");
            return true;
        } else
        {
            System.out.println("Something weird happened: " + result);
            return false;
        }
    }

    @Override
    public boolean stop()
    {
        return false;
    }
}
