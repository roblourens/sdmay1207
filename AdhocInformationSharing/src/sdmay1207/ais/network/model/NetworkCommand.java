package sdmay1207.ais.network.model;

/**
 * The data that is sent to a node on the network as a command. Can be
 * subclassed by 3rd party apps with custom commands (drive car forward, etc.)
 */
public class NetworkCommand
{
    public static enum CommandType
    {
        ReqVideo, ReqImage
    }
    
    
}
