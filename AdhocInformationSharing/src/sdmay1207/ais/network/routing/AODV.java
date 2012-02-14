package sdmay1207.ais.network.routing;

import java.util.Observable;
import java.util.Observer;

import aodv.Node;
import aodv.Node.PacketToObserver;
import aodv.Node.ValueToObserver;
import aodv.ObserverConst;

// interfaces with the AODV bachelor's implementation
// just calls methods in Node, etc. to send data
public class AODV implements RoutingImpl, Observer
{
    private Node nodeAODV;
    
    private static final int TRANSMIT_PKT_ID = 0;
    private static final int BCAST_PKT_ID = 1;

    @Override
    public boolean transmitData(int nodeNumber, String data)
    {
        nodeAODV.sendData(TRANSMIT_PKT_ID, nodeNumber, data.getBytes());
        return true;
    }

    public boolean broadcastData(String data)
    {
        nodeAODV.sendData(BCAST_PKT_ID, BROADCAST_ID, data.getBytes());
        return true;
    }

    @Override
    public boolean start(String subnet, int nodeNumber)
    {
        try
        {
            nodeAODV = new Node(nodeNumber);
            nodeAODV.addObserver(this);
            nodeAODV.startThread();
        }
        // there are like 5 subtypes we could catch here if we want to be
        // detailed
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean stop()
    {
        nodeAODV.stopThread();
        return true;
    }

    @Override
    public void update(Observable sender, Object data)
    {
        if (data instanceof PacketToObserver)
        {
            PacketToObserver pkt = (PacketToObserver) data;

            int type = pkt.getMessageType();
            //int destination = pkt.getSenderNodeAddress();

            switch (type)
            {
            case ObserverConst.DATA_RECEIVED:
                // TODO
                break;
            default:
                System.out.println("Unknown PacketToObserver type");
            }
        } else
        {
            ValueToObserver value = (ValueToObserver) data;
            int type = value.getMessageType();

            switch (type)
            {
            case 0:
                System.out.println("Error: "
                        + ((Integer) value.getContainedData()).toString()
                        + "\tMessage Type: " + value.getMessageType());
                break;
            case 4:
                System.out.println("Found Node: "
                        + ((Integer) value.getContainedData()).toString());
                break;
            case 3:
                System.out.println("Lost Node: "
                        + ((Integer) value.getContainedData()).toString());
                break;
            case 2:
                System.out.println("ACK Received");
                break;
            default:
                System.out.println("Value Response: "
                        + ((Integer) value.getContainedData()).toString()
                        + "\tMessage Type: " + value.getMessageType());
                break;
            }
        }
    }
}
