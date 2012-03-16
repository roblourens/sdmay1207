package sdmay1207.networkrejoining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.etc.Repeater.TimedRepeater;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.GPS.Location;

public class NetworkRejoinMonitor extends TimedRepeater
{
    private NodeController nc;

    private Map<Integer, Node> lastNodes = null;

    private List<NetworkRejoinListener> listeners = new ArrayList<NetworkRejoinListener>();

    public NetworkRejoinMonitor(NodeController nc)
    {
        // routes expire after 3000 atm, this should work for detecting when all
        // nodes are lost?
        super(4000);

        this.nc = nc;
    }

    @Override
    protected void runOnce()
    {
        Map<Integer, Node> nodes = nc.getNodesInNetwork();
        
        if (lastNodes == null)
            return;

        if (nodes.size() < lastNodes.size())
        {
            int numLostNodes = lastNodes.size() - nodes.size();

            // call back to listeners for the appropriate number of lost nodes
            if (numLostNodes == 1)
            {
                for (NetworkRejoinListener l : listeners)
                    l.lostSingleNode();
            } else if (nodes.size() == 0)
            {
                Location goTo = nodesCenterOfGravity(nodes.values());
                for (NetworkRejoinListener l : listeners)
                    l.lostEntireNetwork(goTo);
            } else
            {
                Collection<Node> lostNodes = new HashSet<Node>();
                for (Node n : lastNodes.values())
                    if (!nodes.containsKey(n.nodeNum))
                        lostNodes.add(n);

                Location goTo = nodesCenterOfGravity(lostNodes);
                for (NetworkRejoinListener l : listeners)
                    l.networkSplit(goTo);
            }
        }

        lastNodes = nodes;
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
            latSum += n.lastLocation.latitude;
            longSum += n.lastLocation.longitude;
        }
        
        double latAvg = latSum/nodes.size();
        double longAvg = longSum/nodes.size();
        
        return new Location(latAvg, longAvg);
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
