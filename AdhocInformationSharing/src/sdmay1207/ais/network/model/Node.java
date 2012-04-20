package sdmay1207.ais.network.model;

import java.util.HashSet;
import java.util.Set;

import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

/**
 * Data for a node in the network
 */
public class Node
{
    // Last part of the IP
    public int nodeNum;

    public Set<SensorType> sensors = new HashSet<SensorType>();

    // The last network event received from this node (??)
    public NetworkEvent lastEvent;

    public Heartbeat lastHeartbeat;

    public Location lastLocation;

    public Node(int nodeNum)
    {
        this.nodeNum = nodeNum;
    }

    public Heartbeat getHeartbeat()
    {
        Heartbeat hb = new Heartbeat();
        hb.from = nodeNum;
        return hb;
    }

    public void addSensorType(SensorType st)
    {
        sensors.add(st);
    }

    /**
     * Updates this Node data with information from the given heartbeat
     * 
     * @param hb
     *            the most recently received heartbeat from this node
     */
    public boolean update(Heartbeat hb)
    {
        // basic sanity checks
        if (hb.from != nodeNum)
        {
            System.err.println("Wrong nodeNum");
            return false;
        } else if (lastHeartbeat != null
                && lastHeartbeat.timestamp >= hb.timestamp)
        {
            System.err.println("Can't update with older heartbeat");
            return false;
        }

        lastHeartbeat = hb;
        sensors = hb.sensorOutput.keySet();

        String locationStr = hb.sensorOutput.get(SensorType.GPS);
        if (locationStr != null)
            lastLocation = new Location(locationStr);
        
        return true;
    }

    public String toString()
    {
        return "" + nodeNum;
    }
}
