package sdmay1207.ais.sensors;

import java.util.HashMap;
import java.util.Map;

/**
 * The sensor interface component encapsulates all behavior related to issuing
 * commands to and retrieving data from the sensors.
 */
public class SensorInterface
{
    // If there are more than 10, fix the Heartbeat serializing code
    public enum SensorType
    {
        Location, Thermometer, StillCamera, Video
    }

    public Map<SensorType, Sensor> sensors = new HashMap<SensorType, Sensor>();

    public SensorInterface()
    {
        // Detect available sensors, populate 'sensors' Map, initialize devices
    }
}
