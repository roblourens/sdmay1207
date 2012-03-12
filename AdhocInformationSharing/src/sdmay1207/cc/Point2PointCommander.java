package sdmay1207.cc;

import java.util.List;
import java.util.Map;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.NodeController.CommandHandler;
import sdmay1207.ais.etc.Repeater;
import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.model.NetworkCommand;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import br.zuq.osm.parser.OSMParser;
import br.zuq.osm.parser.model.OSM;
import br.zuq.osm.parser.model.OSMNode;
import br.zuq.osm.parser.model.Way;

public class Point2PointCommander implements CommandHandler
{
    // components
    private NodeController nodeController;
    private Point2PointGUI gui;
    private Point2PointNodeWrangler wrangler;

    // data
    private LocationGraph graph;
    private P2PState curState;
    private Location curDest;
    private GoToLocCommand curCommand;

    public enum P2PState
    {
        inactive, enRoute, searching, waiting, active, enRouteToRallyPoint
    }

    public Point2PointCommander(Point2PointGUI gui,
            NodeController nodeController)
    {
        this.gui = gui;
        this.nodeController = nodeController;

        // Read cached maps from file
        try
        {
            // osm = OSMParser.parse(Device.getDataDir() + "/map.osm");
            OSM osm = OSMParser
                    .parse("/Users/rob/Documents/ISU/senior design/ames.osm");
            graph = getRegionGraph(osm);
            wrangler = new Point2PointNodeWrangler(graph);
        } catch (Exception e)
        {
            System.err.println("XML parsing fail");
            e.printStackTrace();
            return;
        }
    }

    // CommandHandler callback
    public void commandReceived(NetworkCommand command)
    {
        if (command.commandType.equals(GoToLocCommand.GO_TO_LOC_COMMAND_TYPE))
        {
            if (curState != P2PState.inactive)
                System.err
                        .println("Received a go to location command while already fulfulling another command");

            GoToLocCommand locCommand = new GoToLocCommand(command);
            curCommand = locCommand;

            // Tell the GUI to go to the location
            gui.goToLocation(locCommand.loc);
            curDest = locCommand.loc;

            // Start checking whether we are there yet
            new StateCheckTask().start();
        }
    }

    /**
     * Called when the user wants to capture video at one location and transmit
     * it through the network to another location
     * 
     * @param p1
     *            start location (video here)
     * @param p2
     *            end location
     */
    public void initiateP2PTask(Location p1, Location p2)
    {
        Map<Integer, Node> availableNodes = nodeController.getNodesInNetwork();
        List<Location> positions = wrangler.getNodePositionsBetweenPoints(p1,
                p2, availableNodes.size());

        // assign nodes to positions
        Map<Node, Location> assignments = wrangler.assignNodesToPositions(
                availableNodes, positions);

        int headNodeNum = ((Node) Utils.reverseMapLookup(assignments,
                positions.get(0))).nodeNum;
        int tailNodeNum = ((Node) Utils.reverseMapLookup(assignments,
                positions.get(positions.size() - 1))).nodeNum;

        // send position assignments
        // this will also send a command to this node, putting it into the
        // command received loop like all the others
        for (Node n : assignments.keySet())
        {
            Location loc = assignments.get(n);
            GoToLocCommand command = new GoToLocCommand(loc, headNodeNum,
                    tailNodeNum);
            nodeController.sendCommand(command, n.nodeNum);
        }
    }

    private class StateCheckTask extends Repeater
    {
        @Override
        protected void runOnce()
        {
            if (curState == P2PState.enRoute)
            {
                Object currentLocationReading = nodeController
                        .getSensorReading(SensorType.GPS);

                if (currentLocationReading != null)
                {
                    RealWorldLocation currentLocation = new RealWorldLocation(
                            (Location) currentLocationReading);

                    if (currentLocation.withinDeltaOf(curDest))
                    {
                        if (isHeadNode())
                            changeState(P2PState.waiting);
                        else
                            changeState(P2PState.searching);
                    }
                }
            } else if (curState == P2PState.searching)
            {
                // check for 2 connected neighbors, or check for specific nodes?
                Map<Integer, Node> nodes = nodeController.getNodesInNetwork();

                // left location?

                // TODO more here
                if (nodes.size() > 2)
                    changeState(P2PState.waiting);
            } else if (curState == P2PState.waiting)
            {
                // check for head and tail both in the network
                // or some kind of signal from head

                if (isHeadNode())
                {
                    Map<Integer, Node> nodes = nodeController
                            .getNodesInNetwork();
                    if (nodes.containsKey(curCommand.tailNodeNum))
                    {
                        changeState(P2PState.active);
                        // start sending vide or something
                    }
                }
            } else if (curState == P2PState.active)
            {
                // wait for something to go horribly wrong, or the end

                if (isHeadNode())
                {
                    // check for end, depending on how ending video stream works
                }
            }
        }
        
        private void changeState(P2PState newState)
        {
            gui.stateChanged(newState);
            curState = newState;
        }

        private boolean isHeadNode()
        {
            return curCommand.headNodeNum == nodeController.getMe().nodeNum;
        }
    }

    private class GoToLocCommand extends NetworkCommand
    {
        public static final String GO_TO_LOC_COMMAND_TYPE = "p2p_GoToLocation";

        public Location loc;
        public int headNodeNum;
        public int tailNodeNum;

        public GoToLocCommand(Location loc, int headNodeNum, int tailNodeNum)
        {
            super(GO_TO_LOC_COMMAND_TYPE);
            this.loc = loc;
            this.headNodeNum = headNodeNum;
            this.tailNodeNum = tailNodeNum;
        }

        public GoToLocCommand(NetworkCommand command)
        {
            super(GO_TO_LOC_COMMAND_TYPE);

            String[] args = command.commandData.toString().split(";");
            this.loc = new Location(args[0]);
            this.headNodeNum = Integer.parseInt(args[1]);
            this.tailNodeNum = Integer.parseInt(args[2]);
        }

        public String toString()
        {
            return Utils.join(";", super.toString(), loc.toString(),
                    headNodeNum + "", tailNodeNum + "");
        }
    }

    // Get the region as a graph, where a connection between two positions
    // means that it is possible to walk in a straight line between them
    private LocationGraph getRegionGraph(OSM osm)
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

    public interface Point2PointGUI
    {
        public void goToLocation(Location loc);

        // called on each state change
        public void stateChanged(P2PState newState);
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
                + new Point2PointCommander(null, null).wrangler
                        .getNodePositionsBetweenPoints(p1, p2, 15));
    }
}