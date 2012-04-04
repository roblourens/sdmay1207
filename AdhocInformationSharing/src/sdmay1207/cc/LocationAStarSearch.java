package sdmay1207.cc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

    // A* search parameters
    // cost so far
    private Map<LocationNode, Double> g = new HashMap<LocationNode, Double>();

    // heuristic cost (guessed cost between the node and the goal)
    private Map<LocationNode, Double> h = new HashMap<LocationNode, Double>();

    // estimated total cost, g+h (basically just cached values)
    private Map<LocationNode, Double> f = new HashMap<LocationNode, Double>();

    // pathLength-1 preceding nodes are part of the same straight path as this
    private Map<LocationNode, Double> pathLength = new HashMap<LocationNode, Double>();

    // the best predecessor node of this node
    private Map<LocationNode, LocationNode> pred = new HashMap<LocationNode, LocationNode>();

    // for now, only distanceOnly is implemented, and this is probably all we
    // will need
    // distanceOnly: will do a typical shortest-path search with distance as a
    // heuristic
    // hops: will compute the estimated number of jumps (based on
    // previous jump sizes) to use as a heuristic
    // straightPaths: based on the number of straight paths with distance less
    // than MAX_LINE_OF_SIGHT_DIST
    public enum SearchMode
    {
        distanceOnly, hops, straightPaths
    }

    private SearchMode searchMode;

    public LocationAStarSearch(SearchMode searchMode)
    {
        this.searchMode = searchMode;
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

            // calculate h if neighbor has not been found yet
            if (!open.contains(neighbor))
                h.put(neighbor, calculateH(curNode, start, goal));

            // update the scores if needed
            if (!open.contains(neighbor) || newG < g.get(neighbor))
            {
                pred.put(neighbor, curNode);
                g.put(neighbor, newG);
                f.put(neighbor, g.get(neighbor) + h.get(neighbor));
            }

            // make sure that neighbor is in f before it's added to open
            if (!open.contains(neighbor))
                open.add(neighbor);
        }

        return false;
    }

    private boolean straightPath(LocationNode node, LocationNode predNode)
    {
        return false;
    }

    private double calculateG(LocationNode node, LocationNode predNode)
    {
        if (searchMode == SearchMode.distanceOnly)
            return g.get(predNode) + node.distanceTo(predNode);
        else if (searchMode == SearchMode.hops)
            return g.get(predNode) + 1;
        else
            // straightPath
            return straightPath(predNode, node) ? g.get(predNode) : g
                    .get(predNode) + 1;
    }

    private double calculateH(LocationNode node, LocationNode start,
            LocationNode goal)
    {
        if (searchMode == SearchMode.distanceOnly)
            return node.distanceTo(goal);
        else
        {
            // estimate remaining hops based on hops so far for both searchmodes
            int jumps = g.get(node).intValue();
            double jumpDist = start.distanceTo(node) / jumps;

            return node.distanceTo(goal) / jumpDist;
        }
    }

    private List<LocationNode> nodesFromStartToNode(LocationNode node)
    {
        List<LocationNode> path = new ArrayList<LocationNode>();

        while (node != null)
        {
            path.add(0, node);

            // print for debugging in josm.jar
            System.out.print("id:" + node.getId());

            node = pred.get(node);

            // print for debugging in josm.jar
            if (node != null)
                System.out.print("|");
        }
        System.out.println();

        return path;
    }

    private List<Location> pathFromStartToNode(LocationNode node)
    {
        List<Location> path = new ArrayList<Location>();

        for (LocationNode n : nodesFromStartToNode(node))
            path.add(n.getLocation());

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