package sdmay1207.ais.network.model;

import sdmay1207.ais.Config;
import sdmay1207.ais.etc.Utils;

public class NetworkMessage
{
    // ms
    public long timestamp;

    public int from;

    public MessageType messageType;

    // Extra data found in strMessage that wasn't used
    public String[] data;
    
    public static int nodeNumMe = 0;

    /**
     * Builds a NetworkMessage of the correct type
     */
    public static NetworkMessage getMessage(String fromIP, String strMessage)
    {
        String[] messageArgs = strMessage.trim().split(";");
        MessageType messageType = MessageType.values()[Integer
                .parseInt(messageArgs[0])];
        String[] remainingArgs = Utils.arrayCopy(messageArgs, 1,
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
        case TextMessage:
            message = new TextMessage(fromIP, remainingArgs);
            break;
        case ShuttingDown:
            message = new NetworkMessage(fromIP, remainingArgs);
            break;
        default:
            System.err.println("Receieved unknown message type. Message: "
                    + strMessage);
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
            System.err.println("Got a message with an unknown subnet: "
                    + fromIP);
            return;
        }

        if (messageArgs.length < 2)
            System.err.println("Bad message format: Got " + messageArgs.length
                    + " arguments, expected 2");

        try
        {
            timestamp = Long.parseLong(messageArgs[0]);
            from = Integer.parseInt(messageArgs[1]);
        } catch (NumberFormatException e)
        {
            System.err
                    .println("Bad message format: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        data = Utils.arrayCopy(messageArgs, 2, messageArgs.length);
    }

    /**
     * Create an empty network message
     */
    public NetworkMessage()
    {
        timestamp = System.currentTimeMillis();
        from = nodeNumMe;
    }
    
    public String description()
    {
        return toString();
    }

    public String toString()
    {
        return Utils.join(";", "" + messageType.ordinal(), "" + timestamp, ""
                + from);
    }

    // ShuttingDown is to inform others that the node is going offline purposely
    // - "don't look for me"
    public static enum MessageType
    {
        Command, Heartbeat, ShuttingDown, TextMessage
    }
}
