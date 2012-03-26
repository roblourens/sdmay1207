package sdmay1207.cc;

import java.util.ArrayList;
import java.util.Collection;
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
    private Location rallyPoint;
    private long timeout;
    private GoToLocCommand curCommand;

    // inactive: no command given
    // enRoute: to assigned point
    // searching: for 2 neighbors
    // waiting: for streaming start message
    // ready: only head node here - connected to tail node and ready for
    // streaming
    // active: currently streaming/routing/receiving the stream
    // enRouteToRallyPoint: done and going home, or gave up after timeout
    public enum P2PState
    {
        inactive, enRoute, searching, waiting, ready, active,
        enRouteToRallyPoint
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

            curDest = locCommand.loc;
            rallyPoint = locCommand.rallyPoint;
            timeout = locCommand.timeout;

            // Tell the GUI to go to the location
            gui.goToLocation(curDest, rallyPoint, locCommand.headNodeNum,
                    locCommand.tailNodeNum, timeout);

            // Start checking whether we are there yet
            new StateCheckTask().start();
        } else if (command.commandType
                .equals(StartStreamCommand.START_STREAM_COMMAND_TYPE))
        {
            // don't care what state we're in now, go to start streaming state
            changeState(P2PState.active);
        } else if (command.commandType
                .equals(StopStreamCommand.STOP_STREAM_COMMAND_TYPE))
        {
            changeState(P2PState.enRouteToRallyPoint);
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
     * @param rallyPoint
     *            location to go after streaming, or upon giving up after
     *            timeout
     * @param timeoutMS
     *            timeout time in milliseconds
     */
    public void initiateP2PTask(Location p1, Location p2, Location rallyPoint,
            long timeoutMS)
    {
        Collection<Node> allNodes = nodeController.getNodesInNetwork().values();
        Collection<Node> useableNodes = nodesWithLocations(allNodes);

        List<Location> positions = wrangler.getNodePositionsBetweenPoints(p1,
                p2, useableNodes.size());

        // assign nodes to positions
        Map<Node, Location> assignments = wrangler.assignNodesToPositions(
                useableNodes, positions);

        int headNodeNum = ((Node) Utils.reverseMapLookup(assignments,
                positions.get(0))).nodeNum;
        int tailNodeNum = ((Node) Utils.reverseMapLookup(assignments,
                positions.get(positions.size() - 1))).nodeNum;

        long timeoutTime = System.currentTimeMillis() + timeoutMS;
        // send position assignments
        // this will also send a command to this node, putting it into the
        // command received loop like all the others
        for (Node n : assignments.keySet())
        {
            Location loc = assignments.get(n);
            GoToLocCommand command = new GoToLocCommand(loc, rallyPoint,
                    headNodeNum, tailNodeNum, timeoutTime);
            nodeController.sendNetworkMessage(command, n.nodeNum);
        }
    }
    
    // should only be called by head node when streaming will start
    public void streamingStarted()
    {
        StartStreamCommand command = new StartStreamCommand();
        nodeController.broadcastNetworkMessage(command);
    }
    
    // should only be called by head node when streaming will stop
    public void streamingStopped()
    {
        StopStreamCommand command = new StopStreamCommand();
        nodeController.broadcastNetworkMessage(command);
    }

    private Collection<Node> nodesWithLocations(Collection<Node> allNodes)
    {
        List<Node> nodes = new ArrayList<Node>();

        for (Node n : allNodes)
            if (n.lastLocation != null)
                nodes.add(n);

        return nodes;
    }

    private void changeState(P2PState newState)
    {
        gui.stateChanged(newState);
        curState = newState;
    }

    private class StateCheckTask extends Repeater
    {
        @Override
        protected void runOnce()
        {
            if (System.currentTimeMillis() >= timeout && curState != P2PState.active)
                changeState(P2PState.enRouteToRallyPoint);
            else if (curState == P2PState.enRoute)
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
                // don't care if the user leaves this area. They know where it
                // is.

                // check for 2 connected neighbors, or check for specific nodes?
                Map<Integer, Node> nodes = nodeController.getNodesInNetwork();

                if (nodes.size() > 2)
                    changeState(P2PState.waiting);
            } else if (curState == P2PState.waiting)
            {
                // check for head and tail both in the network
                // or some kind of signal from head

                Map<Integer, Node> nodes = nodeController.getNodesInNetwork();
                if (isHeadNode())
                {
                    if (nodes.containsKey(curCommand.tailNodeNum))
                        changeState(P2PState.ready);
                } else
                {
                    if (nodes.size() <= 2)
                        changeState(P2PState.searching);
                }
            } else if (curState == P2PState.active)
            {
                // wait for something to go horribly wrong, or the end
                
                // what happens when the connection is lost while transmitting?

                if (isHeadNode())
                {
                    // check for end, depending on how ending video stream works
                }
            }
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
        public Location rallyPoint;
        public int headNodeNum;
        public int tailNodeNum;
        public long timeout;

        public GoToLocCommand(Location loc, Location rallyPoint,
                int headNodeNum, int tailNodeNum, long timeout)
        {
            super(GO_TO_LOC_COMMAND_TYPE);
            this.loc = loc;
            this.rallyPoint = rallyPoint;
            this.headNodeNum = headNodeNum;
            this.tailNodeNum = tailNodeNum;
            this.timeout = timeout;
        }

        public GoToLocCommand(NetworkCommand command)
        {
            super(GO_TO_LOC_COMMAND_TYPE);

            String[] args = command.commandData.toString().split(";");
            this.loc = new Location(args[0]);
            this.rallyPoint = new Location(args[1]);
            this.headNodeNum = Integer.parseInt(args[2]);
            this.tailNodeNum = Integer.parseInt(args[3]);
            this.timeout = Long.parseLong(args[4]);
        }

        public String toString()
        {
            return Utils.join(";", super.toString(), loc.toString(),
                    rallyPoint.toString(), headNodeNum + "", tailNodeNum + "",
                    timeout + "");
        }
    }

    private class StartStreamCommand extends NetworkCommand
    {
        public static final String START_STREAM_COMMAND_TYPE = "p2p_startStream";

        public StartStreamCommand()
        {
            super(START_STREAM_COMMAND_TYPE);
        }

        public StartStreamCommand(NetworkCommand command)
        {
            super(START_STREAM_COMMAND_TYPE);
        }

        public String toString()
        {
            return Utils.join(";", super.toString());
        }
    }

    private class StopStreamCommand extends NetworkCommand
    {
        public static final String STOP_STREAM_COMMAND_TYPE = "p2p_stopStream";

        public StopStreamCommand()
        {
            super(STOP_STREAM_COMMAND_TYPE);
        }

        public StopStreamCommand(NetworkCommand command)
        {
            super(STOP_STREAM_COMMAND_TYPE);
        }

        public String toString()
        {
            return Utils.join(";", super.toString());
        }
    }

    // Get the region as a graph, where a connection between two positions
    // means that it is possible to walk in a straight line between them
    public static LocationGraph getRegionGraph(OSM osm)
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
        public void goToLocation(Location loc, Location rallyPoint,
                int headNodeNum, int tailNodeNum, long timeout);

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