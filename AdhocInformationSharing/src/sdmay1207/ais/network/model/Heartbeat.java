package sdmay1207.ais.network.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class Heartbeat extends NetworkMessage
{
    public Map<SensorType, String> sensorOutput = new HashMap<SensorType, String>();

    public Heartbeat(String strHeartbeat)
    {
        super(strHeartbeat);

        for (String s : data)
        {
            SensorType st = SensorType.values()[s.charAt(0) - '0'];
            String sensorReading = s.substring(1);

            sensorOutput.put(st, sensorReading);
        }
    }

    public Heartbeat()
    {

    }

    // format like <1 digit sensortype><sensor output><;>
    public String toString()
    {
        StringBuilder sensorOutputStr = new StringBuilder();

        Iterator<Entry<SensorType, String>> it = sensorOutput.entrySet()
                .iterator();
        while (it.hasNext())
        {
            Entry<SensorType, String> entry = it.next();
            sensorOutputStr.append(entry.getKey().ordinal());
            sensorOutputStr.append(entry.getValue());

            if (it.hasNext())
                sensorOutputStr.append(";");
        }

        return super.toString() + ";" + sensorOutputStr;
    }
}