package sdmay1207.ais.network;

import java.util.List;

import sdmay1207.ais.network.NetworkController.Heartbeat;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.sensors.SensorInterface.HeartbeatSensorType;
import sdmay1207.ais.sensors.SensorInterface.OnDemandSensorType;

/**
 * Data for a node in the network
 */
public class Node
{
    // Last part of the IP
    public int nodeNum;
    
    public List<HeartbeatSensorType> heartBeatSensors;
    
    public List<OnDemandSensorType> onDemandSensors;
    
    // The last network event received from this node 
    public NetworkEvent lastEvent; 
    
    public Heartbeat lastHeartbeat;
    
    public Node(int nodeNum)
    {
        
    }
}
