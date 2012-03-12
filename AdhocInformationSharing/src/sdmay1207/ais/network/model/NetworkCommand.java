package sdmay1207.ais.network.model;

import java.util.Arrays;

import sdmay1207.ais.etc.Utils;

/**
 * The data that is sent to a node on the network as a command. Can be
 * subclassed by 3rd party apps with custom commands (drive car forward, etc.)
 */
public class NetworkCommand extends NetworkMessage
{
    // Defined by the command implementor, so arbitrary command types are
    // allowed
    public String commandType;

    // Extra optional data, probably not used if NetworkCommand is subclassed
    public Object commandData;
    
    public NetworkCommand(String commandType)
    {
        
    }

    public NetworkCommand(String commandType, Object commandData)
    {
        messageType = MessageType.Command;
        this.commandType = commandType;
        this.commandData = commandData;
    }

    /**
     * Constructor to build a NetworkCommand model object from received data
     * 
     * @param fromIP
     *            The IP address that sent this command
     * @param heartbeatArgs
     *            The (;-separated) data associated with this command
     */
    public NetworkCommand(String fromIP, String[] commandArgs)
    {
        super(fromIP, commandArgs);
        messageType = MessageType.Command;

        if (data.length < 2)
        {
            System.err.println("Malformed NetworkCommand received: "
                    + Arrays.toString(commandArgs));
            return;
        }
        
        commandType = data[0];
        commandData = data[1];
    }

    public NetworkCommand()
    {
        super();
        messageType = MessageType.Command;
    }

    public String toString()
    {
        if (commandData != null)
            return Utils.join(";", super.toString(), commandType,
                    commandData.toString());
        else
            return Utils.join(";", super.toString(), commandType);
    }
}