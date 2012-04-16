package sdmay1207.ais.network.routing;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import sdmay1207.ais.network.NetworkController.Receiver;
import aoa.aodv.Node;
import aoa.aodv.ObserverConst;
import aoa.aodv.Node.PacketToObserver;
import aoa.aodv.Node.ValueToObserver;

// interfaces with the AODV bachelor's implementation
// just calls methods in Node, etc. to send data
public class AODV implements RoutingImpl, Observer
{
    private Node nodeAODV;
    private Receiver receiver;
    private String subnet;

    private static final int TRANSMIT_PKT_ID = 0;
    private static final int BCAST_PKT_ID = 1;

    public AODV(Receiver receiver)
    {
        this.receiver = receiver;
    }

    public boolean transmitData(int nodeNumber, byte[] data)
    {
        nodeAODV.sendData(TRANSMIT_PKT_ID, nodeNumber, data);
        return true;
    }

    @Override
    public boolean transmitData(int nodeNumber, String data)
    {
        return transmitData(nodeNumber, data.getBytes());
    }

    public boolean broadcastData(String data)
    {
        nodeAODV.sendData(BCAST_PKT_ID, BROADCAST_ID, data.getBytes());
        return true;
    }

    public boolean broadcastData(byte[] data)
    {
        nodeAODV.sendData(BCAST_PKT_ID, BROADCAST_ID, data);
        return true;
    }

    @Override
    public boolean start(String subnet, int nodeNumber)
    {
        this.subnet = subnet;

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

    public Set<Integer> getZeroHopNeighbors()
    {
        return nodeAODV.getZeroHopNeighbors();
    }

    // updated by AODV
    @Override
    public void update(Observable sender, Object data)
    {
        if (data instanceof PacketToObserver)
        {
            PacketToObserver pkt = (PacketToObserver) data;

            int type = pkt.getMessageType();
            int source = pkt.getSenderNodeAddress();

            if (type != ObserverConst.DATA_RECEIVED)
                System.err.println("Unknown PacketToObserver type");
            else
                receiver.addMessageData(subnet + source,
                        (byte[]) pkt.getContainedData());
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
                // Ignore what AODV thinks about nodes joining or leaving
                // because it often wrong!
                // int newNodeNum = (Integer) value.getContainedData();
                // receiver.nodeJoined(newNodeNum);
                break;
            case 3:
                // int lostNodeNum = (Integer) value.getContainedData();
                // receiver.nodeLeft(lostNodeNum);
                break;
            case 2:
                System.out.println("ACK Received");
                break;
            default:
                System.out.println("Value Response: "
                        + ((Integer) value.getContainedData()).toString()
                        + "\tMessage Type: " + type);
                break;
            }
        }
    }
}
