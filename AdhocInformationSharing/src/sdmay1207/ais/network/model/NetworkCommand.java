package sdmay1207.ais.network.model;

/**
 * The data that is sent to a node on the network as a command. Can be
 * subclassed by 3rd party apps with custom commands (drive car forward, etc.)
 */
public class NetworkCommand extends NetworkMessage
{
    public enum CommandType
    {
        GetImage, StartVideoStream, StopVideoStream
    }

    public CommandType commandType;

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