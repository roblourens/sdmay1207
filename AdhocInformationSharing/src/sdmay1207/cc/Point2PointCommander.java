package sdmay1207.cc;

import java.util.List;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.sensors.GPS.Location;

public class Point2PointCommander
{
    private NodeController nodeController;

    // Consider this to be the furthest that a connection can be made with
    // roughly a line of sight, in meters
    private static final int MAX_LINE_OF_SIGHT_DIST = 150;

    public Point2PointCommander(NodeController nodeController)
    {
        this.nodeController = nodeController;

        // Read cached maps from file
    }

    /**
     * Returns a list of Locations which should be able to form a connected
     * network between Locations p1 and p2, per the maps of these areas
     * 
     * @param n
     *            The number of nodes to form the network
     * @return
     */
    public List<Location> getNodePositionsBetweenPoints(Location p1,
            Location p2, int n)
    {
        // Get the region as a graph, where a connection between two positions
        // means that wireless communication should be possible between them
        LocationGraph graph = getRegionGraph();

        // Perform a search over the graph - find the least-hop path between the
        // two positions, with distance to the goal as a heuristic
        LocationAStarSearch search = new LocationAStarSearch(false);
        List<Location> positions = search.findPath(null, null);
        
        // If positions.size() < n, figure out where to assign the extra nodes
        
        return positions;
    }

    // maybe takes params for specifying a subregion
    public LocationGraph getRegionGraph()
    {
        return null;
    }
}