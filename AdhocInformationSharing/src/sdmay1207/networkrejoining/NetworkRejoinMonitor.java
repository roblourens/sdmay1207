package sdmay1207.networkrejoining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.etc.Repeater.TimedRepeater;
import sdmay1207.ais.network.NetworkController.Event;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.GPS.Location;

public class NetworkRejoinMonitor extends TimedRepeater implements Observer
{
    private NodeController nc;

    private Collection<Node> lostNodes = new HashSet<Node>();

    // Numbers to ignore during this cycle, because they are shutting down on
    // purpose
    private Collection<Integer> ignoreNums = new HashSet<Integer>();

    private List<NetworkRejoinListener> listeners = new ArrayList<NetworkRejoinListener>();

    public NetworkRejoinMonitor(NodeController nc)
    {
        // routes expire after 3000 atm, this should work for detecting when all
        // nodes are lost?
        super(5000);

        this.nc = nc;
        nc.addNetworkObserver(this);
    }

    @Override
    protected void runOnce()
    {
        for (Node n : lostNodes)
            System.out.println(n.nodeNum);

        int numLostNodes = lostNodes.size();
        if (numLostNodes > 0)
        {
            System.out.println("lost: " + numLostNodes);
            int numTotalNodes = nc.getNodesInNetwork().size();

            // call back to listeners for the appropriate number of lost nodes
            if (numLostNodes == 1)
            {
                System.out.println("Lost 1 node");
                for (NetworkRejoinListener l : listeners)
                    l.lostSingleNode();
            } else if (numTotalNodes == 0)
            {
                System.out.println("Lost all nodes");
                Location goTo = nodesCenterOfGravity(lostNodes);
                for (NetworkRejoinListener l : listeners)
                    l.lostEntireNetwork(goTo);
            } else
            {
                System.out.println("Split: lost " + numLostNodes + " nodes");
                Location goTo = nodesCenterOfGravity(lostNodes);
                for (NetworkRejoinListener l : listeners)
                    l.networkSplit(goTo);
            }
        }

        lostNodes.clear();
        ignoreNums.clear();
    }

    public void addListener(NetworkRejoinListener l)
    {
        listeners.add(l);
    }

    // this will not work around the prime meridian!
    // e.g. longs of -170 and 180
    private Location nodesCenterOfGravity(Collection<Node> nodes)
    {
        double latSum = 0;
        double longSum = 0;

        for (Node n : nodes)
        {
            if (n.lastLocation == null)
            {
                System.out.println("Skipping node " + n.nodeNum
                        + ", no location known");
                continue;
            }

            latSum += n.lastLocation.latitude;
            longSum += n.lastLocation.longitude;
        }

        double latAvg = latSum / nodes.size();
        double longAvg = longSum / nodes.size();

        return new Location(latAvg, longAvg);
    }

    // event received from the NetworkController
    @Override
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        if (netEvent.event == Event.NodeLeft)
        {
            System.out.println("NRM got lost node msg: " + netEvent.data);

            if (!ignoreNums.contains(netEvent.data))
            {
                Node lostNode = nc.getKnownNodes().get(netEvent.data);
                if (lostNode != null && !lostNodes.contains(lostNode))
                    lostNodes.add(lostNode);
                else
                    System.err
                            .println("Strange... NRM can't find that lost node");
            } else
                System.out.println("Ignoring loss of node " + netEvent.data);
        } else if (netEvent.event == Event.RecvdShuttingDownMessage)
        {
            NetworkMessage msg = (NetworkMessage) netEvent.data;
            System.out.println("Ignoring impending node loss: " + msg.from);
            ignoreNums.add(msg.from);
        } else if (netEvent.event == Event.NodeJoined)
        {
            // don't ignore it anymore
            ignoreNums.remove(netEvent.data);

            // if it left and rejoined within this cycle, remove from lost list
            Node joinedNode = nc.getKnownNodes().get(netEvent.data);
            if (lostNodes.contains(joinedNode))
                lostNodes.remove(joinedNode);
        }
    }

    // GUI implements this, maybe this class passes instructions some how to the
    // GUI for what to do for each situation

    // 2 ways to do this:
    // the GUI could listen to network events and be updated whenever a node
    // leaves so it can display that event however it wants
    // or these methods can pass a list of the lost nodes

    // The locations passed are recommendations on where to go. We won't monitor
    // it or enforce it, just advise the user to go in that direction, and they
    // can ignore or whatever
    public interface NetworkRejoinListener
    {
        // lost one node - they can deal
        public void lostSingleNode();

        // lost 1<n<all nodes - two halves should head towards each other
        public void networkSplit(Location p);

        // lost all nodes - head toward all others
        public void lostEntireNetwork(Location p);
    }
}
