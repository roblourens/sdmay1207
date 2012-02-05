package sdmay1207.ais.network.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class Heartbeat extends NetworkMessage
{
    public Map<SensorType, String> sensorOutput = new HashMap<SensorType, String>();

    /**
     * Constructor to build a Heartbeat model object from received data
     * 
     * @param fromIP
     *            The IP address that sent this heartbeat
     * @param heartbeatArgs
     *            The (;-separated) data associated with this heartbeat message
     */
    public Heartbeat(String fromIP, String[] heartbeatArgs)
    {
        super(fromIP, heartbeatArgs);

        messageType = MessageType.Heartbeat;
        for (String s : data)
        {
            SensorType st = SensorType.values()[s.charAt(0) - '0'];
            String sensorReading = s.substring(1);

            sensorOutput.put(st, sensorReading);
        }
    }

    /**
     * Empty constructor to build a new Heartbeat for sending
     */
    public Heartbeat()
    {
        super();
        messageType = MessageType.Heartbeat;
    }

    // format like <1 digit sensortype><sensor output><;>
    public String toString()
    {
        StringBuilder sensorOutputSB = new StringBuilder();

        Iterator<Entry<SensorType, String>> it = sensorOutput.entrySet()
                .iterator();
        while (it.hasNext())
        {
            Entry<SensorType, String> entry = it.next();
            sensorOutputSB.append(entry.getKey().ordinal());
            sensorOutputSB.append(entry.getValue());

            if (it.hasNext())
                sensorOutputSB.append(";");
        }

        String sensorOutputStr = sensorOutputSB.toString();
        String result = super.toString();
        
        if (!sensorOutputStr.isEmpty())
            result += ";" + sensorOutputStr;
        
        return result;
    }
}