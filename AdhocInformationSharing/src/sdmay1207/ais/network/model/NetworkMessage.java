package sdmay1207.ais.network.model;

import java.util.Arrays;

import sdmay1207.ais.etc.Utils;

public abstract class NetworkMessage
{
    // ms
    public long timestamp;

    public int from;

    public MessageType messageType;

    // Extra data found in strMessage that wasn't used
    public String[] data; 

    /**
     * Deserialize the message from a String
     * @param strMessage
     */
    public NetworkMessage(String strMessage)
    {
        String[] message = strMessage.split(";");
        if (message.length < 3)
            System.err.println("Bad message format: Got " + message.length
                    + " arguments, expected 3");

        try
        {
            messageType = MessageType.values()[Integer.parseInt(message[0])];
            timestamp = Long.parseLong(message[1]);
            from = Integer.parseInt(message[2]);
        } catch (NumberFormatException e)
        {
            System.err
                    .println("Bad message format: " + e.getLocalizedMessage());
            System.err.println(e.getStackTrace());
        }
        
        data = Arrays.copyOfRange(message, 3, message.length);
    }

    /**
     * Create an empty network message
     */
    public NetworkMessage()
    {

    }

    public String toString()
    {
        return Utils.join(";", "" + messageType.ordinal(), "" + timestamp, ""
                + from);
    }

    public static enum MessageType
    {
        Command, Heartbeat
    }
}
