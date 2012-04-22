package sdmay1207.ais.network.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sdmay1207.ais.Device;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import sdmay1207.cc.Point2PointCommander.P2PState;

public class Heartbeat extends NetworkMessage
{
    public Map<SensorType, String> sensorOutput = new HashMap<SensorType, String>();

    public P2PState taskState;

    public boolean canSendVideo;

    public boolean canReceiveVideo;

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
            char code = s.charAt(0);
            if (code == 't')
                taskState = P2PState.values()[Integer.parseInt(s.substring(1))];
            else if (code == 's')
                canSendVideo = Integer.parseInt(s.substring(1)) == 1;
            else if (code == 'r')
                canReceiveVideo = Integer.parseInt(s.substring(1)) == 1;
            else
            {
                SensorType st = SensorType.values()[code - '0'];
                String sensorReading = s.substring(1);

                sensorOutput.put(st, sensorReading);
            }
        }
    }

    /**
     * Empty constructor to build a new Heartbeat for sending
     */
    public Heartbeat()
    {
        super();
        messageType = MessageType.Heartbeat;
        canSendVideo = Device.isAndroidSystem();
        canReceiveVideo = !Device.isAndroidSystem();
    }

    // format like <1 digit sensortype><sensor output>;...
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

        if (!sensorOutputStr.equals(""))
            result += ";" + sensorOutputStr;

        if (taskState != null)
            result += ";t" + taskState.ordinal();

        result += ";s" + (canSendVideo ? 1 : 0);
        result += ";r" + (canReceiveVideo ? 1 : 0);

        return result;
    }
}