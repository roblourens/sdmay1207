package sdmay1207.ais.network.model;

import java.util.Arrays;

import sdmay1207.ais.Config;
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
     * Builds a NetworkMessage of the correct type
     */
    public static NetworkMessage getMessage(String fromIP, String strMessage)
    {
        System.out.println("Constructing message from String: " + strMessage);
        
        String[] messageArgs = strMessage.split(";");
        MessageType messageType = MessageType.values()[Integer
                .parseInt(messageArgs[0])];
        String[] remainingArgs = Arrays.copyOfRange(messageArgs, 1,
                messageArgs.length);

        NetworkMessage message = null;
        switch (messageType)
        {
        case Command:
            message = new NetworkCommand(fromIP, remainingArgs);
            break;
        case Heartbeat:
            message = new Heartbeat(fromIP, remainingArgs);
            break;
        }

        return message;
    }

    public static NetworkMessage getMessage(String fromIP, byte[] messageData)
    {
        return getMessage(fromIP, new String(messageData));
    }

    /**
     * Deserialize the message from a String
     * 
     * @param strMessage
     */
    public NetworkMessage(String fromIP, String[] messageArgs)
    {
        // break the nodeNumber out of the fromIP
        if (!fromIP.startsWith(Config.SUBNET))
        {
            System.err.println("Got a message with an unknown subnet");
            return;
        }
        from = Integer.parseInt(fromIP.split("\\.")[3]);

        if (messageArgs.length < 2)
            System.err.println("Bad message format: Got " + messageArgs.length
                    + " arguments, expected 2");

        try
        {

            timestamp = Long.parseLong(messageArgs[1]);
            from = Integer.parseInt(messageArgs[2]);
        } catch (NumberFormatException e)
        {
            System.err
                    .println("Bad message format: " + e.getLocalizedMessage());
            System.err.println(e.getStackTrace());
        }

        data = Arrays.copyOfRange(messageArgs, 2, messageArgs.length);
    }

    /**
     * Create an empty network message
     */
    public NetworkMessage()
    {
        timestamp = System.currentTimeMillis();
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
