package sdmay1207.ais;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import sdmay1207.ais.network.NetworkController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.SensorInterface;

/**
 * The node controller component implements node logic by controlling the other
 * components. It will decide when to transmit sensor data to other nodes in the
 * network and what to do with data received from other nodes in the network. It
 * will receive commands from the GUI and decide how to complete them.
 * 
 * The node controller will also provide the ability for other applications to
 * work with the network. It will provide an interface which allows for
 * retrieving information about the network, sending arbitrary data, such as
 * commands, to other nodes, and other functionality useful for external
 * applications.
 */
public class NodeController implements Observer
{
    NetworkController networkController;
    SensorInterface sensorInterface = new SensorInterface();
    Node me;

    public NodeController(int nodeNumber, RoutingAlg routingAlg)
    {
        me = new Node(nodeNumber);
        networkController = new NetworkController(nodeNumber, routingAlg);
        networkController.addObserver(this);
    }

    public void start()
    {
        new Timer().schedule(new HeartbeatTask(), HEARTBEAT_FREQ);
    }

    // ms
    private static final int HEARTBEAT_FREQ = 5000;

    // probably won't usually be used - GUI should call the constructor instead
    public static void main(String[] args)
    {
        args = new String[]{"4"};
        if (args.length < 1)
            throw new RuntimeException("Must provide the node number");

        int nodeNumber = 0;
        try
        {
            nodeNumber = Integer.parseInt(args[0]);
            if (nodeNumber < 1 || nodeNumber > 254)
                throw new RuntimeException("Node number " + nodeNumber
                        + " out of bounds");
        } catch (NumberFormatException e)
        {
            throw new RuntimeException("Go to hell");
        }

        new NodeController(nodeNumber, RoutingAlg.BATMAN).start();
    }

    // event received from the NetworkController
    @Override
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;

        switch (netEvent.event)
        {
        case RecvdHeartbeat:
            Heartbeat hb = (Heartbeat) netEvent.data;
            // do something useful with it- pass to GUI or something
            // or GUI has actually registered as the listener
            break;
        }
    }

    public class HeartbeatTask extends TimerTask
    {
        @Override
        public void run()
        {
            networkController.sendHeartbeat(me.getHeartbeat());
            
            new Timer().schedule(this, HEARTBEAT_FREQ);
        }
    }
}