package sdmay1207.ais.network.model;

public class ShuttingDownMessage extends NetworkMessage
{
    public ShuttingDownMessage()
    {
        messageType = MessageType.ShuttingDown;
    }
    
    public ShuttingDownMessage(String fromIP, String[] commandArgs)
    {
        super(fromIP, commandArgs);
        messageType = MessageType.ShuttingDown;
    }
    
    public String description()
    {
        return "Node " + from + " is shutting down on purpose";
    }
}
