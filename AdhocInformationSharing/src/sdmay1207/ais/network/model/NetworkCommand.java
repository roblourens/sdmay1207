package sdmay1207.ais.network.model;

/**
 * The data that is sent to a node on the network as a command. Can be
 * subclassed by 3rd party apps with custom commands (drive car forward, etc.)
 */
public class NetworkCommand extends NetworkMessage
{
    // Defined by the command implementor, so arbitrary command types are
    // allowed
    public String commandType;
    
    // Extra data, e.g. location
    public String commandData;
    
    public NetworkCommand(String commandType, String commandData)
    {
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
    }

    public NetworkCommand()
    {
        super();
    }

    public String toString()
    {
        return super.toString(); // + ";" + command type, etc.
    }
}