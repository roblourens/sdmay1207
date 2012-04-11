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
    private static final int MAX_LINE_OF_SIGHT_DIST = 200;

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

            locPrefs.put(position, thisPositionPrefs);
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
            positions.add(0, p1);

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
            Location p2, int n, boolean straighten)
    {
        List<Location> positions = getPathBetweenPoints(p1, p2);
        int initialNum = positions.size();
        if (straighten)
        {
            removeRedundantPositions(positions);
            System.out.println("Straightening removed "
                    + (initialNum - positions.size()) + " nodes");
        }

        System.out.println("Need " + positions.size()
                + " nodes before filling in long segments, have " + n);

        // Not enough nodes to cover all corners - if we eventually consider
        // known obstacles, this is one place it would be useful
        if (positions.size() > n)
            return null;

        int extraNodes = n - positions.size();

        // weird edge case, just get it out of here
        if (positions.size() == 1)
        {
            // assign all nodes to that point
            for (int i = 0; i < extraNodes; i++)
                positions.add(positions.get(0));
        }

        int[] extras = extraNodeAssignments(positions, extraNodes);

        if (!haveEnoughNodes(positions, extras, extraNodes))
        {
            System.out.println("Too large gaps");
            return null;
        }

        return placeExtraNodes(positions, extras);
    }

    private int[] extraNodeAssignments(List<Location> positions, int extraNodes)
    {
        // extras[i] is the number of extra nodes assigned to the gap between
        // positions i and i+1
        int[] extras = new int[positions.size() - 1];

        // assign all extras
        while (extraNodes > 0)
        {
            double maxGap = -1;
            int maxGapStart = -1;

            // find max gap
            for (int i = 0; i < positions.size() - 1; i++)
            {
                Location l1 = positions.get(i);
                Location l2 = positions.get(i + 1);

                if (l1.distanceTo(l2) / (extras[i] + 1) > maxGap)
                {
                    maxGap = l1.distanceTo(l2) / (extras[i] + 1);
                    maxGapStart = i;
                }
            }

            // assign an extra node to the largest gap
            extras[maxGapStart]++;
            extraNodes--;
        }

        return extras;
    }

    private boolean haveEnoughNodes(List<Location> positions, int[] extras,
            int extraNodes)
    {
        // ensure that the distance between any two nodes is less than
        // MAX_LINE_OF_SIGHT_DIST
        for (int i = 0; i < positions.size() - 1; i++)
        {
            Location l1 = positions.get(i);
            Location l2 = positions.get(i + 1);

            // too large gap still remains - not enough nodes to cover
            if (l1.distanceTo(l2) / (extras[i] + 1) > MAX_LINE_OF_SIGHT_DIST)
                return false;
        }

        return true;
    }

    private List<Location> placeExtraNodes(List<Location> positions,
            int[] extras)
    {
        // positions with gaps filled
        List<Location> filledPositions = new ArrayList<Location>();

        // now place extras
        for (int i = 0; i < extras.length; i++)
        {
            int numExtras = extras[i];
            Location gap1 = positions.get(i);
            Location gap2 = positions.get(i + 1);

            // add the beginning of the gap then the split nodes. (the end of
            // the gap is the beginning of the next gap, don't add here)
            filledPositions.add(gap1);
            for (int j = 0; j < numExtras; j++)
            {
                double splitLat = gap1.latitude + (j + 1)
                        * (gap2.latitude - gap1.latitude) / (numExtras + 1);
                double splitLon = gap1.longitude + (j + 1)
                        * (gap2.longitude - gap1.longitude) / (numExtras + 1);
                Location split = new Location(splitLat, splitLon);
                filledPositions.add(split);
            }
        }

        // the last node isn't the beginning of any gap, so add it here
        filledPositions.add(positions.get(positions.size() - 1));
        return filledPositions;
    }

    private final int REASONABLE_SMALL_ANGLE = 15; // deg

    // Looks at the nodes 3 at a time. If the group is close to a straight line,
    // then the middle is removed
    private void removeRedundantPositions(List<Location> positions)
    {
        for (int i = 0; i < positions.size() - 2; i++)
        {
            Location p0 = positions.get(i);
            Location p1 = positions.get(i + 1);
            Location p2 = positions.get(i + 2);

            Vector v0 = new Vector(p0, p1);
            Vector v2 = new Vector(p2, p1);

            // angle between the two lines, 180 == straight
            double theta = Math.acos(v0.dotProduct(v2)
                    / (v0.magnitude() * v2.magnitude()))
                    * 180 / Math.PI;

            // close to straight?
            if (180 - theta < REASONABLE_SMALL_ANGLE)
            {
                // remove p1
                positions.remove(i + 1);

                // to check from p0 to p3 with p2 as middle
                i--;
            }
        }
    }

    private class Vector
    {
        private double x;
        private double y;

        public Vector(Location p0, Location p1)
        {
            this.x = p0.longitude - p1.longitude;
            this.y = p0.latitude - p1.latitude;
        }

        public double magnitude()
        {
            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        }

        public double dotProduct(Vector v)
        {
            return x * v.x + y * v.y;
        }
    }
}