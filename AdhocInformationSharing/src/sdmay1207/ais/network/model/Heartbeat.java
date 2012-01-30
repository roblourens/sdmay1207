package sdmay1207.ais.network.model;

import sdmay1207.ais.network.Node;
import sdmay1207.ais.sensors.GPS.GPSReading;

public class Heartbeat
{
    public Node from;

    // Could be json here
    public String sensorOutput;

    public GPSReading location;

    // ms
    public long timestamp;
    
    // for sending over the network
    // could use JSON or something simpler
    public String toString()
    {
        return "";
    }
}