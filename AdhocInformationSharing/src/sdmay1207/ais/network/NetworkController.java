package sdmay1207.ais.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sdmay1207.ais.etc.Repeater;
import sdmay1207.ais.etc.Repeater.TimedRepeater;
import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.NetworkMessage;
import sdmay1207.ais.network.model.Node;

/**
 * The network controller component encapsulates all behavior related to
 * deciding how and when to connect to a network, and managing the way data is
 * exchanged between the application and the network.
 */
public class NetworkController extends Observable
{
    public NetworkInterface networkInterface;
    public Receiver r;

    private boolean isRunning = false;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private String keyHex = "8a8492e4a036b29809fb8968d591cea0";
    private NodeConnectivityMonitor ncm;
    private int nodeNumber;

    // Note: node objects between knownNodes/connectedNodes are the same
    // A map of all nodes which have been seen so far - at this point, may or
    // may not include this node
    private Map<Integer, Node> knownNodes = new ConcurrentHashMap<Integer, Node>();

    // only nodes which are currently connected
    private Map<Integer, Node> connectedNodes = new ConcurrentHashMap<Integer, Node>();

    public enum Event
    {
        NodeJoined, NodeLeft, RecvdHeartbeat, RecvdData, RecvdCommand,
        RecvdTextMessage, RecvdShuttingDownMessage, SentHeartbeat, ACK
    }

    /**
     * A message object returned to observers on each network event.
     */
    public static class NetworkEvent
    {
        public Event event;

        public Object data;

        public long rcvdTimestamp;

        public NetworkEvent(Event event, Object data)
        {
            this.event = event;
            this.data = data;
            this.rcvdTimestamp = System.currentTimeMillis();
        }
    }

    public NetworkController()
    {
        try
        {
            SecretKeySpec skeySpec = new SecretKeySpec(
                    Utils.hexStrToBytes(keyHex), "AES");

            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec);

            r = new Receiver();
            networkInterface = new NetworkInterface(r);
            ncm = new NodeConnectivityMonitor(r);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Turn on the adhoc network, start the routing system
     */
    public boolean start(int nodeNumber, RoutingAlg routingAlg)
    {
        this.nodeNumber = nodeNumber;
        
        knownNodes = new ConcurrentHashMap<Integer, Node>();
        connectedNodes = new ConcurrentHashMap<Integer, Node>();
        
        isRunning = networkInterface.startNetwork(nodeNumber)
                && networkInterface.startRouting(routingAlg);

        if (isRunning)
        {
            r.start();
            ncm.start();
        } else
            System.err.println("NetworkInterface did not start up properly");

        return isRunning;
    }

    /**
     * Shut off the network, stop the routing system
     */
    public void stop()
    {
        if (networkInterface != null)
            networkInterface.stop();

        r.stop();
        ncm.stop();

        isRunning = false;
    }

    /**
     * Broadcasts the heartbeat object
     */
    public boolean sendHeartbeat(Heartbeat hb)
    {
        byte[] encrypted = encryptedData(hb.toString().getBytes());
        return networkInterface.broadcastData(encrypted);
    }

    /**
     * Sends the command to the specified node
     */
    public boolean sendNetworkMessage(NetworkMessage message, int destNodeNum)
    {
        return networkInterface.sendData(destNodeNum, encryptedData(message
                .toString().getBytes()));
    }

    public boolean broadcastNetworkMessage(NetworkMessage message)
    {
        return networkInterface.broadcastData(encryptedData(message.toString()
                .getBytes()));
    }

    public boolean transmitDataUnencrypted(int destNodeNum, byte[] data)
    {
        return networkInterface.sendData(destNodeNum, data);
    }

    /**
     * Returns all connected nodes. Depends on which nodes we've received a
     * heartbeat from, not which nodes the routing algorithm has detected
     * 
     * @return a list of all nodes in the entire mesh network that this node can
     *         currently connect to.
     */
    public Map<Integer, Node> getNodesInNetwork()
    {
        return connectedNodes;
    }

    /**
     * Returns all nodes which have ever been seen
     */
    public Map<Integer, Node> getKnownNodes()
    {
        return knownNodes;
    }

    /**
     * @return a list of all nodes that this node is directly connected to
     */
    public Map<Integer, Node> neighborNodes()
    {
        // not running? return an empty map
        if (!isRunning)
            return new HashMap<Integer, Node>();

        Set<Integer> neighbors = networkInterface.routingImpl
                .getZeroHopNeighbors();
        Map<Integer, Node> neighborMap = new HashMap<Integer, Node>();

        for (Integer neighborAddr : neighbors)
        {
            Node neighborNode = connectedNodes.get(neighborAddr);
            if (neighborNode == null)
                System.err
                        .println("Problem: node "
                                + neighborAddr
                                + " isn't connected according to the NetworkController");
            else
                neighborMap.put(neighborAddr, neighborNode);
        }

        return neighborMap;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Update the map of known nodes with the info from the given Heartbeat
     * object. Returns true if the node was not previously in connectedNodes or
     * knownNodes.
     */
    private boolean handleHeartbeat(Heartbeat hb)
    {
        boolean nodeIsNew = false;

        if (knownNodes.get(hb.from) == null)
        {
            knownNodes.put(hb.from, new Node(hb.from));
            nodeIsNew = true;
        }

        if (knownNodes.get(hb.from).update(hb))
            sendHeartbeat(hb);

        if (!connectedNodes.containsKey(hb.from))
        {
            connectedNodes.put(hb.from, knownNodes.get(hb.from));
            nodeIsNew = true;
        }

        return nodeIsNew;
    }

    private byte[] encryptedData(byte[] data)
    {

        byte[] encrypted = new byte[0];
        try
        {
            encrypted = encryptCipher.doFinal(data);
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }

        return data;
    }

    private byte[] decryptedData(byte[] data)
    {
        byte[] original = new byte[0];
        try
        {
            original = decryptCipher.doFinal(data);
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }

        return original;
    }

    /**
     * Receives data/events from the network, interprets it into the correct
     * message
     * 
     * @author rob
     * 
     */
    public class Receiver extends Repeater
    {
        private Queue<NetworkEvent> receivedEvents;
        private DatagramSocket localSock;

        private final int CAM_STREAM_PORT = 7476;
        private final InetAddress CAM_STREAM_ADDR;

        public Receiver()
        {
            try
            {
                CAM_STREAM_ADDR = InetAddress.getByName("127.0.0.1");
            } catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(
                        "Could not set up camera stream InetAddress");
            }
        }

        @Override
        public void start()
        {
            System.out.println("Receiver starting");
            receivedEvents = new ConcurrentLinkedQueue<NetworkEvent>();
            try
            {
                localSock = new DatagramSocket();
            } catch (SocketException e)
            {
                e.printStackTrace();
                throw new RuntimeException(
                        "Could not set up local camera socket");
            }

            super.start();
        }

        @Override
        public void stop()
        {
            System.out.println("Receiver stopping");
            localSock.close();

            super.stop();
        }

        @Override
        protected void runOnce()
        {
            synchronized (receivedEvents)
            {
                while (receivedEvents.isEmpty())
                {
                    try
                    {
                        receivedEvents.wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            NetworkEvent event = receivedEvents.poll();
            setChanged();
            notifyObservers(event);
        }

        public void addMessageData(String fromIP, byte[] data)
        {
            // is camera data?
            if (data[0] == -128 && (data[1] == 74 || data[1] == -54))
            {
                handleCameraStreamPacket(data);
                return;
            }

            // data = decryptedData(data);
            if (data.length == 0)
            {
                System.err
                        .println("Received data was empty or could not be decrypted - dropping");
                return;
            }

            NetworkMessage msg = NetworkMessage.getMessage(fromIP, data);
            addMessage(msg);
        }

        public void addMessage(NetworkMessage msg)
        {
            NetworkEvent event = null;

            switch (msg.messageType)
            {
            case Command:
                event = new NetworkEvent(Event.RecvdCommand, msg);
                break;
            case Heartbeat:
                event = new NetworkEvent(Event.RecvdHeartbeat, msg);
                if (handleHeartbeat((Heartbeat) msg))
                    nodeJoined(msg.from);
                break;
            case TextMessage:
                event = new NetworkEvent(Event.RecvdTextMessage, msg);
                break;
            case ShuttingDown:
                event = new NetworkEvent(Event.RecvdShuttingDownMessage, msg);
                break;
            }

            addEvent(event);
        }

        public void ackReceived(Object o)
        {
            addEvent(new NetworkEvent(Event.ACK, o.toString()));
        }

        public void nodeLeft(int nodeNumber)
        {
            connectedNodes.remove(nodeNumber);
            addEvent(new NetworkEvent(Event.NodeLeft, nodeNumber));
        }

        // Let's only add nodes to connected when we get a heartbeat
        public void nodeJoined(int nodeNumber)
        {
            addEvent(new NetworkEvent(Event.NodeJoined, nodeNumber));
        }

        /**
         * Add the event to the queue, then wake the notifer thread
         */
        public void addEvent(NetworkEvent event)
        {
            receivedEvents.add(event);
            synchronized (receivedEvents)
            {
                receivedEvents.notify();
            }
        }

        public void handleCameraStreamPacket(byte[] data)
        {
            try
            {
                System.out.println("Sending camera packet to "
                        + CAM_STREAM_ADDR + ", port " + CAM_STREAM_PORT);
                localSock.send(new DatagramPacket(data, data.length,
                        CAM_STREAM_ADDR, CAM_STREAM_PORT));
            } catch (IOException e)
            {
                e.printStackTrace();
                System.err.println("Problem sending camera data");
            }
        }
    }

    private class NodeConnectivityMonitor extends TimedRepeater
    {
        private Receiver r;

        public NodeConnectivityMonitor(Receiver r)
        {
            super(1000);
            this.r = r;
        }

        @Override
        protected void runOnce()
        {
            for (Node n : getNodesInNetwork().values())
            {
                // 3*heartbeat rate, ms
                if (n.lastHeartbeat.rcvdTime < System.currentTimeMillis() - 6000
                        && n.nodeNum != nodeNumber)
                {
                    System.out.println("Lost node " + n.nodeNum + " at "
                            + System.currentTimeMillis() + " last hb at "
                            + n.lastHeartbeat.rcvdTime);
                    r.nodeLeft(n.nodeNum);
                }
            }
        }
    }
}