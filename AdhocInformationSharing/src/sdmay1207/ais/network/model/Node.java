package sdmay1207.ais.network.model;

import java.util.List;

import sdmay1207.ais.network.NetworkController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

/**
 * Data for a node in the network
 */
public class Node
{
    // Last part of the IP
    public int nodeNum;

    public List<SensorType> sensors;

    // The last network event received from this node
    public NetworkEvent lastEvent;

    public Heartbeat lastHeartbeat;

    public Node(int nodeNum)
    {

    }

    public Heartbeat getHeartbeat()
    {
        Heartbeat hb = new Heartbeat();

        hb.timestamp = System.currentTimeMillis();
        return hb;
    }
}
