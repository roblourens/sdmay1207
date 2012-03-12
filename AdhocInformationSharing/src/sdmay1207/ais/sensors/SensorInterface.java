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
        GPS, Thermometer, StillCamera, Video
    }

    public Map<SensorType, Sensor> sensors = new HashMap<SensorType, Sensor>();

    public void addSensor(Sensor s)
    {
        if (sensors.keySet().contains(s.getType()))
        {
            System.err.println("Trying to add duplicate sensor type "
                    + s.getType());
            return;
        }

        sensors.put(s.getType(), s);
    }
    
    public Object getReading(SensorType sensorType)
    {
        return sensors.get(sensorType).getReading();
    }
}
