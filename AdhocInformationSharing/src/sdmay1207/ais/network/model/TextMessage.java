package sdmay1207.ais.network.model;

import java.util.Arrays;

import sdmay1207.ais.etc.Utils;

public class TextMessage extends NetworkMessage
{
    public String message;

    public TextMessage()
    {
        this("");
    }

    public TextMessage(String msg)
    {
        this.message = msg;
    }

    public TextMessage(String fromIP, String[] commandArgs)
    {
        super(fromIP, commandArgs);
        messageType = MessageType.TextMessage;

        if (data.length < 1)
        {
            System.err.println("Malformed TextMessage received: "
                    + Arrays.toString(commandArgs));
            return;
        }
        
        message = data[0];
    }

    public String toString()
    {
        return Utils.join(";", super.toString(), message);
    }
}
