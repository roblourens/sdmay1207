package sdmay1207.cc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.LocationAStarSearch.SearchMode;
import sdmay1207.cc.LocationGraph.LocationNode;

public class Point2PointNodeWrangler
{
    private LocationGraph graph;

    // Consider this to be the furthest that a connection can be made with
    // roughly a line of sight, in meters
    private static final int MAX_LINE_OF_SIGHT_DIST = 150;

    public Point2PointNodeWrangler(LocationGraph graph)
    {
        this.graph = graph;
    }

    public Map<Node, Location> assignNodesToPositions(
            Collection<Node> availableNodes, List<Location> positions)
    {
        Map<Node, List<Location>> nodePrefs = new HashMap<Node, List<Location>>();
        Map<Location, List<Node>> locPrefs = new HashMap<Location, List<Node>>();

        // Collect node prefs
        for (final Node node : availableNodes)
        {
            List<Location> thisNodePrefs = new ArrayList<Location>(positions);

            // Sort all positions by distance to the node
            Collections.sort(thisNodePrefs, new Comparator<Location>()
            {
                @Override
                public int compare(Location p1, Location p2)
                {
                    double d1 = node.lastLocation.distanceTo(p1);
                    double d2 = node.lastLocation.distanceTo(p2);

                    if (d1 < d2)
                        return -1;
                    else if (d2 < d1)
                        return 1;
                    else
                        return 0;
                }
            });

            nodePrefs.put(node, thisNodePrefs);
        }

        // Collect position prefs
        for (final Location position : positions)
        {
            List<Node> thisPositionPrefs = new ArrayList<Node>(availableNodes);

            // Sort all nodes by distance to the position
            Collections.sort(thisPositionPrefs, new Comparator<Node>()
            {
                @Override
                public int compare(Node n1, Node n2)
                {
                    double d1 = position.distanceTo(n1.lastLocation);
                    double d2 = position.distanceTo(n2.lastLocation);

                    if (d1 < d2)
                        return -1;
                    else if (d2 > d1)
                        return 1;
                    else
                        return 0;
                }
            });
        }

        Map<Location, Node> locationAssignments = new HashMap<Location, Node>();
        List<Node> freeNodes = new ArrayList<Node>(availableNodes);
        while (freeNodes.size() > 0)
        {
            Node node = freeNodes.remove(0);

            for (Location p : nodePrefs.get(node))
            {
                if (locationAssignments.get(p) == null)
                {
                    locationAssignments.put(p, node);
                    break;
                } else
                {
                    Node other = locationAssignments.get(p);
                    List<Node> thisLocPrefs = locPrefs.get(p);

                    if (thisLocPrefs.indexOf(node) < thisLocPrefs
                            .indexOf(other))
                    {
                        freeNodes.add(other);
                        locationAssignments.put(p, node);
                        break;
                    }
                }
            }
        }

        // Reverse the map and return
        Map<Node, Location> assignments = new HashMap<Node, Location>();
        for (Location p : locationAssignments.keySet())
            assignments.put(locationAssignments.get(p), p);

        return assignments;
    }

    public List<Location> getPathBetweenPoints(Location p1, Location p2)
    {
        // Setup the search algorithm to perform a shortest-path A* search
        LocationAStarSearch search = new LocationAStarSearch(
                SearchMode.distanceOnly);

        LocationNode ln1 = graph.getBestNodeForLocation(p1);
        LocationNode ln2 = graph.getBestNodeForLocation(p2);
        List<Location> positions = search.findPath(ln1, ln2);

        // Add the exact start/end points if they are not too close to the first
        // actual graph nodes
        if (!ln1.withinDeltaOf(p1))
            positions.add(p1);

        if (!ln2.withinDeltaOf(p2))
            positions.add(p2);

        return positions;
    }

    /**
     * Returns a list of Locations which should be able to form a connected
     * network between Locations p1 and p2, per the maps of these areas. Returns
     * null if impossible (distance too far, etc.)
     * 
     * @param n
     *            The number of nodes to form the network
     * @return
     */
    public List<Location> getNodePositionsBetweenPoints(Location p1,
            Location p2, int n)
    {
        List<Location> positions = getPathBetweenPoints(p1, p2);
        //positions = removeRedundantPositions(positions);

        // Not enough nodes to cover all corners - if we eventually consider
        // known obstacles, this is one place it would be useful
        if (positions.size() > n)
            return null;

        // TODO eliminate redundant nodes in straight-line paths and nodes which
        // are very close to each other

        // If positions.size() < n, figure out where to assign the extra nodes,
        // and ensure that the distance between any two nodes is less than
        // MAX_LINE_OF_SIGHT_DIST
        int extraNodes = n - positions.size();
        for (int i = 0; i < positions.size() - 1; i++)
        {
            Location l1 = positions.get(i);
            Location l2 = positions.get(i + 1);

            if (l1.distanceTo(l2) > MAX_LINE_OF_SIGHT_DIST)
            {
                // enough nodes to cover this gap?
                if (extraNodes < 1)
                    return null;
                else
                {
                    Location split = new Location(
                            (l1.latitude + l2.latitude) / 2,
                            (l1.longitude + l2.longitude) / 2);
                    positions.add(i + 1, split);
                    i--; // need to check between l1 and split now
                    extraNodes--;
                }
            }
        }

        // now assign any extras to the largest gaps
        while (extraNodes > 0)
        {
            double maxGap = Double.MIN_VALUE;
            int maxGapStart = -1;

            for (int i = 0; i < positions.size() - 1; i++)
            {
                Location l1 = positions.get(i);
                Location l2 = positions.get(i + 1);

                if (l1.distanceTo(l2) > maxGap)
                {
                    maxGap = l1.distanceTo(l2);
                    maxGapStart = i;
                }
            }

            Location gap1 = positions.get(maxGapStart);
            Location gap2 = positions.get(maxGapStart + 1);
            Location split = new Location((gap1.latitude + gap2.latitude) / 2,
                    (gap1.longitude + gap2.longitude) / 2);
            positions.add(maxGapStart, split);
            extraNodes--;
        }

        return positions;
    }

    private final int REASONABLE_LARGE_DIST = 120; // m

    // Looks at the nodes 3 at a time. If the group is close to a straight line,
    // and the distance between the two on the end is less than
    // REASONABLE_LARGE_DIST, then the middle is removed
    private List<Location> removeRedundantPositions(List<Location> positions)
    {
        for (int i=0; i<positions.size()-2; i++)
        {
            Location p0 = positions.get(i);
            Location p1 = positions.get(i+1);
            Location p2 = positions.get(i+2);
            
            
        }
        
        return positions;
    }
}
