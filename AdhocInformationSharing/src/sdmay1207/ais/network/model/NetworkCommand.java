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
    public String[] commandData;

    public NetworkCommand(String commandType)
    {
        super();
        messageType = MessageType.Command;
        this.commandType = commandType;
    }

    public NetworkCommand(String commandType, Object commandData)
    {
        super();
        messageType = MessageType.Command;
        this.commandType = commandType;
        this.commandData = commandData.toString().split(";");
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
        commandData = Utils.arrayCopy(data, 1, data.length);
    }

    // should be overridden by the subclass
    public String toString()
    {
        return Utils.join(";", super.toString(), commandType);
    }
}