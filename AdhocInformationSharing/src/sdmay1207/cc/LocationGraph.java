package sdmay1207.cc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.sensors.GPS.Location;

public class LocationGraph
{
    private Map<Location, LocationNode> vertices = new HashMap<Location, LocationNode>();

    /**
     * Adds an edge between EXACTLY the two coordinates given
     */
    public void addExactEdge(Location p1, Location p2)
    {
        addExactEdge(p1, p2, "", "");
    }
    
    public void addExactEdge(Location p1, Location p2, String id1, String id2)
    {
        if (!vertices.containsKey(p1))
            vertices.put(p1, new LocationNode(p1, id1));

        if (!vertices.containsKey(p2))
            vertices.put(p2, new LocationNode(p2, id2));

        vertices.get(p1).addNeighbor(vertices.get(p2));
        vertices.get(p2).addNeighbor(vertices.get(p1));
    }

    /**
     * Determines whether there are existing nodes within DELTA*2 of the given
     * coordinates. If so, adds an edge between those, otherwise, is the same as
     * addExactEdge
     */
    public void addApproxEdge(Location p1, Location p2)
    {
        LocationNode n1 = getApproxNodeForLocation(p1);
        LocationNode n2 = getApproxNodeForLocation(p2);

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

    public boolean containsExactPoint(Location p)
    {
        return vertices.containsKey(p);
    }

    public boolean containsApproxPoint(Location p)
    {
        return getApproxNodeForLocation(p) != null;
    }

    public LocationNode getExactNodeForLocation(Location p)
    {
        return vertices.get(p);
    }

    // This is a crappy solution but it's probably fast enough for us
    public LocationNode getApproxNodeForLocation(Location p)
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

    private LocationNode getAnyNode()
    {
        return vertices.values().iterator().next();
    }

    private LocationNode getNextBestNode(List<LocationNode> path)
    {
        return null;
    }

    /**
     * Returns a simplified version of this graph, such that every path which is
     * close to a straight line is reduced to the 2 nodes which are the
     * endpoints of the straight line
     */
    public LocationGraph simple()
    {
        // look at each neighbor of curNode - which to take? pick 1

        // repeat for every neighbor of the endpoints of the line - pick the
        // node that minimizes the error of the line between endpoints -
        // determine the distance from each point to the line, square it (or
        // more), sum and compare to some constant acceptable value

        LocationGraph lg = new LocationGraph();
        LocationNode startNode = getAnyNode();

        // Set<LocationNode> closed = new HashSet<LocationNode>();
        Queue<LocationNode> q = new LinkedList<LocationNode>();
        q.add(startNode);

        // Loops when the current path has closed
        while (!q.isEmpty())
        {
            List<LocationNode> path = new ArrayList<LocationNode>();
            LocationNode curNode = q.poll(); // path start
            path.add(curNode);
            // closed.add(curNode);
            // add all curNode neighors to q

            while (true)
            {
                LocationNode nextNode = getNextBestNode(path);
                // add neighbors to q

                // Path complete?
                if (nextNode == null)
                    break;
            }

            // add the endpoints to the graph
        }

        return lg;
    }

    class LocationNode
    {
        // If two Locations are within DELTA meters, consider them equal
        public static final double DELTA = 5;

        private Location loc;

        private Set<LocationNode> neighbors = new HashSet<LocationNode>();
        
        private String id;

        public LocationNode(Location loc)
        {
            this(loc, "");
        }
        
        public LocationNode(Location loc, String id)
        {
            this.loc = loc;
            this.id = id;
        }

        public Location getLocation()
        {
            return loc;
        }
        
        public String getId()
        {
            return id;
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

        public boolean equals(Object o)
        {
            if (!(o instanceof LocationNode))
                return false;

            LocationNode ln = (LocationNode) o;
            return loc.equals(ln.loc);
        }

        public String toString()
        {
            return id + ": " + loc.toString();
        }
    }
}