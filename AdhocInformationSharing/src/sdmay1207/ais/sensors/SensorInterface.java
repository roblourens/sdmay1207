package sdmay1207.ais.sensors;

import java.util.ArrayList;
import java.util.List;

/**
 * The sensor interface component encapsulates all behavior related to issuing
 * commands to and retrieving data from the sensors.
 */
public class SensorInterface
{
    public enum HeartbeatSensorType
    {
        location, thermometer
    }

    public enum OnDemandSensorType
    {
        stillCamera, video
    }

    /** GPS, battery, basic stuff */
    private List<Sensor> heartbeatSensors = new ArrayList<Sensor>();

    /** Camera, etc. */
    private List<Sensor> onDemandSensors = new ArrayList<Sensor>();
}
