package sdmay1207.cc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.LocationGraph.LocationNode;

public class LocationAStarSearch
{
    private Queue<LocationNode> open;

    private Set<LocationNode> closed = new HashSet<LocationNode>();

    private Map<LocationNode, Double> g = new HashMap<LocationNode, Double>();
    private Map<LocationNode, Double> h = new HashMap<LocationNode, Double>();
    private Map<LocationNode, Double> f = new HashMap<LocationNode, Double>();

    private Map<LocationNode, LocationNode> pred = new HashMap<LocationNode, LocationNode>();

    // If true, will do a typical shortest-path search with distance as a
    // heuristic. If false, will compute the estimated number of jumps (based on
    // previous jump sizes) to use as a heuristic
    private boolean considerDistanceOnly = true;

    public LocationAStarSearch(boolean considerDistanceOnly)
    {
        this.considerDistanceOnly = considerDistanceOnly;
    }

    public List<Location> findPath(LocationNode start, LocationNode goal)
    {
        open = new PriorityQueue<LocationNode>(10, new DistanceHeuristic());

        open.add(start);
        g.put(start, 0D);
        h.put(start, start.distanceTo(goal));
        f.put(start, g.get(start) + h.get(start));

        while (!open.isEmpty())
        {
            LocationNode curNode = open.poll();

            if (visitNode(curNode, start, goal))
                return pathFromStartToNode(curNode);
        }

        return null;
    }

    private boolean visitNode(LocationNode curNode, LocationNode start,
            LocationNode goal)
    {
        if (curNode.withinDeltaOf(goal))
            return true;

        closed.add(curNode);
        for (LocationNode neighbor : curNode.getNeighbors())
        {
            if (closed.contains(neighbor))
                continue;

            double newG = calculateG(neighbor, curNode);
            boolean updateScores = false;
            if (!open.contains(neighbor))
            {
                open.add(neighbor);
                h.put(neighbor, calculateH(curNode, start, goal));
                updateScores = true;
            } else if (newG < g.get(neighbor))
                updateScores = true;

            if (updateScores)
            {
                pred.put(neighbor, curNode);
                g.put(neighbor, newG);
                f.put(neighbor, g.get(neighbor) + h.get(neighbor));
            }
        }

        return false;
    }

    private double calculateG(LocationNode node, LocationNode predNode)
    {
        if (considerDistanceOnly)
            return g.get(predNode) + node.distanceTo(predNode);
        else
            return g.get(predNode) + 1;
    }

    private double calculateH(LocationNode node, LocationNode start,
            LocationNode goal)
    {
        if (considerDistanceOnly)
            return node.distanceTo(goal);
        else
        {
            int jumps = g.get(node).intValue();
            double jumpDist = start.distanceTo(node) / jumps;

            return node.distanceTo(goal) / jumpDist;
        }
    }

    private List<Location> pathFromStartToNode(LocationNode node)
    {
        List<Location> path = new LinkedList<Location>();

        while (node != null)
        {
            path.add(0, node.getLocation());
            node = pred.get(node);
        }

        return path;
    }

    public class DistanceHeuristic implements Comparator<LocationNode>
    {
        @Override
        public int compare(LocationNode n1, LocationNode n2)
        {
            double f1 = f.get(n1);
            double f2 = f.get(n2);

            if (f1 < f2)
                return -1;
            else if (f2 < f1)
                return 1;
            else
                return 0;
        }
    }
}