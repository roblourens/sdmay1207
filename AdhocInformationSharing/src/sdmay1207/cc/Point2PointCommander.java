package sdmay1207.cc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.NodeController.CommandHandler;
import sdmay1207.ais.network.model.NetworkCommand;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.LocationAStarSearch.SearchMode;
import sdmay1207.cc.LocationGraph.LocationNode;
import br.zuq.osm.parser.OSMParser;
import br.zuq.osm.parser.model.OSM;
import br.zuq.osm.parser.model.OSMNode;
import br.zuq.osm.parser.model.Way;

public class Point2PointCommander implements CommandHandler
{
    private NodeController nodeController;

    private OSM osm;

    public static final String P2P_COMMAND_TYPE = "p2p_GoToLocation";

    // Consider this to be the furthest that a connection can be made with
    // roughly a line of sight, in meters
    private static final int MAX_LINE_OF_SIGHT_DIST = 5;

    public Point2PointCommander(NodeController nodeController)
    {
        this.nodeController = nodeController;

        // Read cached maps from file
        try
        {
            // osm = OSMParser.parse(Device.getDataDir() + "/map.osm");
            osm = OSMParser
                    .parse("/Users/rob/Documents/ISU/senior design/ames.osm");
        } catch (Exception e)
        {
            System.err.println("XML parsing fail");
            e.printStackTrace();
            return;
        }
        
        // Register for P2P commands on the network
        nodeController.registerForCommand(P2P_COMMAND_TYPE, this);
    }
    
    public void commandReceived(NetworkCommand command)
    {
        
    }

    public void initiateP2PTask(Location p1, Location p2)
    {
        Map<Integer, Node> availableNodes = nodeController.getNodesInNetwork();
        List<Location> positions = getNodePositionsBetweenPoints(p1, p2,
                availableNodes.size());

        // assign nodes to positions
        Map<Node, Location> assignments = assignNodesToPositions(
                availableNodes, positions);

        // send position assignments
        // this will also send a command to this node, putting it into the
        // command received loop like all the others
        for (Node n : assignments.keySet())
            nodeController.sendCommand(P2P_COMMAND_TYPE, assignments.get(n)
                    .toString(), n.nodeNum);
    }

    private Map<Node, Location> assignNodesToPositions(
            Map<Integer, Node> availableNodes, List<Location> positions)
    {
        Map<Node, List<Location>> nodePrefs = new HashMap<Node, List<Location>>();
        Map<Location, List<Node>> locPrefs = new HashMap<Location, List<Node>>();

        // Collect node prefs
        for (final Node node : availableNodes.values())
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
            List<Node> thisPositionPrefs = new ArrayList<Node>(
                    availableNodes.values());

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
        List<Node> freeNodes = new ArrayList<Node>(availableNodes.values());
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

    /**
     * Returns a list of Locations which should be able to form a connected
     * network between Locations p1 and p2, per the maps of these areas. Returns
     * null if impossible (distance too far, etc.)
     * 
     * @param n
     *            The number of nodes to form the network
     * @return
     */
    private List<Location> getNodePositionsBetweenPoints(Location p1,
            Location p2, int n)
    {
        // Get the region as a graph, where a connection between two positions
        // means that it is possible to walk in a straight line between them
        LocationGraph graph = getRegionGraph();

        // Setup the search algorithm to perform a shortest-path A* search
        LocationAStarSearch search = new LocationAStarSearch(
                SearchMode.distanceOnly);

        LocationNode ln1 = graph.getApproxNodeForLocation(p1);
        LocationNode ln2 = graph.getApproxNodeForLocation(p2);
        List<Location> positions = search.findPath(ln1, ln2);

        // Add the exact start/end points if they are not too close to the first
        // actual graph nodes
        if (!ln1.withinDeltaOf(p1))
            positions.add(p1);

        if (!ln2.withinDeltaOf(p2))
            positions.add(p2);

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

            for (int i = 0; i < positions.size(); i++)
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
        }

        return positions;
    }

    // maybe takes params for specifying a subregion
    private LocationGraph getRegionGraph()
    {
        LocationGraph lg = new LocationGraph();

        for (Way way : osm.getWays())
        {
            for (int i = 0; i < way.nodes.size() - 1; i++)
            {
                OSMNode node1 = way.nodes.get(i);
                OSMNode node2 = way.nodes.get(i + 1);

                Location p1 = new Location(Double.parseDouble(node1.lat),
                        Double.parseDouble(node1.lon));
                Location p2 = new Location(Double.parseDouble(node2.lat),
                        Double.parseDouble(node2.lon));
                lg.addExactEdge(p1, p2, node1.id, node2.id);
            }
        }

        return lg;
    }

    public static void main(String[] args)
    {
        // Location p1 = new Location(42.024676, -93.646598);
        // Location p2 = new Location(42.028914, -93.641515);

        // Location p1 = new Location(42.011887, -93.651653);
        // Location p2 = new Location(42.050751, -93.615111);

        // Location p1 = new Location(42.042122, -93.620496);
        // Location p2 = new Location(42.050498, -93.614815);
        // Location p2 = new Location(42.051201, -93.618004);

        Location p1 = new Location(42.0228962, -93.6714000);
        Location p2 = new Location(42.0228070, -93.6638790);

        System.out.println("Result: "
                + new Point2PointCommander(null).getNodePositionsBetweenPoints(
                        p1, p2, 15));
    }
}