package sdmay1207.ais.network.routing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import sdmay1207.ais.Device;
import sdmay1207.ais.etc.Repeater;
import sdmay1207.ais.etc.Repeater.TimedRepeater;
import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.NetworkController.Receiver;

// wraps a socket to send data using BATMAN
public class BATMAN implements RoutingImpl
{
    private static final int SEND_PORT = 1207; // ?
    private static final int RECV_PORT = 1208;

    private DatagramSocket sendSock;
    private Receiver receiver;
    private String interfaceName;
    private String subnet;

    private UDPReceiver udpReceiver;
    private ConnectedNodeMonitor connectedNodeMonitor;

    public BATMAN(Receiver receiver, String dataDir, String interfaceName)
    {
        this.receiver = receiver;
        this.interfaceName = interfaceName;

        try
        {
            sendSock = new DatagramSocket(SEND_PORT);
        } catch (SocketException e)
        {
            System.out.println("BATMAN could not create a socket");
            e.printStackTrace();
        }

        // for now, assume BATMAN is installed
    }

    @Override
    public boolean start(String subnet, int nodeNumber)
    {
        this.subnet = subnet;

        if (Device.isAndroidSystem())
        {
            /*String result = Device.sysCommand("su -c \"" + Device.getDataDir()
                    + "/lib/batmand " + interfaceName + "\"");*/
            Device.sysCommand("su -c \"/data/data/adhoc/batmand " + interfaceName + "\"");
        } else
        {
            String result = Device.sysCommand("sudo batmand " + interfaceName);
            if (result.startsWith("Not using") || result.startsWith("Error"))
            {
                System.out.println("batmand failed to start: " + result);
                return false;
            } else if (result.startsWith("Interface activated"))
            {
                System.out.println("batmand started successfully");
            } else
            {
                System.out.println("Something weird happened: " + result);
                return false;
            }
        }

        udpReceiver = new UDPReceiver(nodeNumber);
        udpReceiver.start();

        connectedNodeMonitor = new ConnectedNodeMonitor();
        connectedNodeMonitor.start();

        return true;
    }

    @Override
    public boolean transmitData(int nodeNumber, String data)
    {

        return false;
    }

    @Override
    public boolean broadcastData(String data)
    {
        InetAddress IPAddress;
        try
        {
            IPAddress = InetAddress.getByName(subnet + BROADCAST_ID);
            DatagramPacket sendPacket;
            sendSock.setBroadcast(true);
            byte[] dataBytes = data.getBytes();
            sendPacket = new DatagramPacket(dataBytes, dataBytes.length,
                    IPAddress, RECV_PORT);
            sendSock.send(sendPacket);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
            return false;
        } catch (SocketException e)
        {
            e.printStackTrace();
            return false;
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean stop()
    {
        // TODO Android? Detect failure?
        Device.sysCommand("sudo killall batmand");

        sendSock.close();
        udpReceiver.stop();
        connectedNodeMonitor.stop();

        return true;
    }

    /**
     * Returns a Set of BATMAN's current one-hop neighbor node numbers
     */
    public Set<Integer> getZeroHopNeighbors()
    {
        Set<Integer> neighbors = new HashSet<Integer>();

        String output = Device.sysCommand("sudo batmand -c -b -d 1");
        String[] lines = output.split("\n");

        // First line is the header
        for (int i = 1; i < lines.length; i++)
        {
            String line = lines[i];

            // Extract (ip)whitespace(ip)something
            line.trim();
            String[] lineValues = line.split("\\s*");

            if (lineValues[0].equals(lineValues[1]))
                neighbors.add(Utils.getNodeNumberFromIP(lineValues[0]));
        }

        return neighbors;
    }

    public Set<Integer> getConnectedNodes()
    {
        Set<Integer> neighbors = new HashSet<Integer>();

        String output;
        if (Device.isAndroidSystem())
            output = Device.sysCommand("su -c \"/data/data/adhoc/batmand -c -b -d 1\"");
        else
            output = Device.sysCommand("sudo batmand -c -b -d 1");
        
        String[] lines = output.split("\n");

        // First line is the header
        for (int i = 1; i < lines.length; i++)
        {
            String line = lines[i];

            // Extract (ip)whitespace(ip)something
            line.trim();
            if (!line.startsWith("No batman nodes in range"))
            {
                String[] lineValues = line.split("\\s*");
                neighbors.add(Utils.getNodeNumberFromIP(lineValues[0]));
            }
        }

        return neighbors;
    }

    private class ConnectedNodeMonitor extends TimedRepeater
    {
        private Set<Integer> connectedNodes = null;

        private static final int CHECK_INTERVAL = 5000; // ms
        
        public ConnectedNodeMonitor()
        {
            super(CHECK_INTERVAL);
        }

        @Override
        public void runOnce()
        {
            Set<Integer> newConnectedNodes = getConnectedNodes();

            if (connectedNodes != null)
            {
                // Find new nodes
                for (Integer nodeNum : newConnectedNodes)
                    if (!connectedNodes.contains(nodeNum))
                        receiver.nodeJoined(nodeNum);

                // Find lost nodes
                for (Integer nodeNum : connectedNodes)
                    if (!newConnectedNodes.contains(nodeNum))
                        receiver.nodeLeft(nodeNum);
            }

            connectedNodes = newConnectedNodes;

            if (connectedNodes.size() != 0)
                System.out.println("BATMAN has " + connectedNodes.size()
                        + " neighbors");
        }
    }

    private class UDPReceiver extends Repeater
    {
        private DatagramSocket dgramSock;
        private int exclude;

        /**
         * @param exclude
         *            Ignores packets from this node number
         */
        public UDPReceiver(int exclude)
        {
            this.exclude = exclude;
            try
            {
                dgramSock = new DatagramSocket(RECV_PORT);
            } catch (SocketException e)
            {
                e.printStackTrace();
                throw new RuntimeException(
                        "Could not set up the receiver socket: network problems");
            }
        }

        @Override
        protected void runOnce()
        {
            try
            {
                // 52kb buffer
                byte[] buffer = new byte[52000];
                DatagramPacket receivedPacket = new DatagramPacket(buffer,
                        buffer.length);

                dgramSock.receive(receivedPacket);

                String fromAddress = receivedPacket.getAddress().toString();

                // toString returns hostname / ip
                String fromIP = fromAddress.split("/")[1];
                if (exclude != Utils.getNodeNumberFromIP(fromIP))
                    receiver.addMessage(fromIP, receivedPacket.getData());
            } catch (IOException e)
            {
                e.printStackTrace();
                System.err
                        .println("Bad things happened in BATMAN message receiver thread");
            }
        }
    }
}
