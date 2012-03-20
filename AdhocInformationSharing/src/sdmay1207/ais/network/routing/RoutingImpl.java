package sdmay1207.ais.network.routing;

import java.util.Set;

// something useful for AODV and BATMAN to interface with this
public interface RoutingImpl
{
    static final int BROADCAST_ID = 255;
    
    public boolean transmitData(int nodeNumber, byte[] data);
    
    public boolean transmitData(int nodeNumber, String data);

    public boolean broadcastData(String data);

    public boolean start(String subnet, int nodeNumber);

    public boolean stop();
    
    public Set<Integer> getZeroHopNeighbors();
    
    // could also get all connected nodes, if needed
}
