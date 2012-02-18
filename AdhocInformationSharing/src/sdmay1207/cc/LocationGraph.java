package sdmay1207.cc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.sensors.GPS.Location;

public class LocationGraph
{
    private Map<Location, LocationNode> vertices = new HashMap<Location, LocationNode>();

    public void addExactEdge(Location p1, Location p2)
    {
        if (!vertices.containsKey(p1))
            vertices.put(p1, new LocationNode(p1));

        if (!vertices.containsKey(p2))
            vertices.put(p2, new LocationNode(p2));

        vertices.get(p1).addNeighbor(vertices.get(p2));
        vertices.get(p2).addNeighbor(vertices.get(p1));
    }

    public void addApproxEdge(Location p1, Location p2)
    {
        LocationNode n1 = getNodeForLocation(p1);
        LocationNode n2 = getNodeForLocation(p2);

        if (n1 == null)
        {
            n1 = new LocationNode(p1);
            vertices.put(p1, n1);
        }

        if (n2 == null)
        {
            n2 = new LocationNode(p2);
            vertices.put(p2, n2);
        }

        n1.addNeighbor(n2);
        n2.addNeighbor(n1);
    }

    public boolean containsPoint(Location p)
    {
        return getNodeForLocation(p) != null;
    }

    private LocationNode getNodeForLocation(Location p)
    {
        // Only necessary if we might have nodes within DELTA*2 of each other
        LocationNode best = null;
        double bestDist = -1;

        for (LocationNode node : vertices.values())
        {
            double dist = node.distanceTo(p);
            if (dist <= LocationNode.DELTA)
            {
                if (best == null || dist < bestDist)
                {
                    best = node;
                    bestDist = dist;
                }
            }
        }

        return best;
    }

    class LocationNode
    {
        // If two Locations are within DELTA meters, consider them equal
        public static final double DELTA = 5;

        private Location loc;

        private Set<LocationNode> neighbors = new HashSet<LocationNode>();

        public LocationNode(Location loc)
        {
            this.loc = loc;
        }
        
        public Location getLocation()
        {
            return loc;
        }
        
        public Set<LocationNode> getNeighbors()
        {
            return neighbors;
        }

        public void addNeighbor(LocationNode neighbor)
        {
            neighbors.add(neighbor);
        }

        public boolean withinDeltaOf(LocationNode other)
        {
            return withinDeltaOf(other.loc);
        }

        public boolean withinDeltaOf(Location p)
        {
            return distanceTo(p) <= DELTA;
        }

        // in meters
        public double distanceTo(LocationNode other)
        {
            return distanceTo(other.loc);
        }

        // in meters
        public double distanceTo(Location p)
        {
            return Utils.distance(this.loc, p);
        }
    }
}