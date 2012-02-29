package sdmay1207.cc;

import java.util.List;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.LocationAStarSearch.SearchMode;
import sdmay1207.cc.LocationGraph.LocationNode;
import br.zuq.osm.parser.OSMParser;
import br.zuq.osm.parser.model.OSM;
import br.zuq.osm.parser.model.OSMNode;
import br.zuq.osm.parser.model.Way;

public class Point2PointCommander
{
    private NodeController nodeController;

    private OSM osm;

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
        // Get the region as a graph, where a connection between two positions
        // means that it is possible to walk in a straight line between them
        LocationGraph graph = getRegionGraph();

        // Setup the search algorithm to perform a shortest-path A* search
        LocationAStarSearch search = new LocationAStarSearch(
                SearchMode.distanceOnly);

        LocationNode ln1 = graph.getApproxNodeForLocation(p1);
        LocationNode ln2 = graph.getApproxNodeForLocation(p2);
        List<Location> positions = search.findPath(ln1, ln2);
        
        // Add the exact start/end points if they are not too close to the first actual graph nodes
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