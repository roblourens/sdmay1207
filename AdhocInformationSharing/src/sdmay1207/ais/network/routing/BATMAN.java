package sdmay1207.ais.network.routing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import sdmay1207.ais.Device;
import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.network.NetworkController.Receiver;

// wraps a socket to send data using BATMAN
public class BATMAN implements RoutingImpl
{
    private static final int SEND_PORT = 1207; // ?
    private static final int RECV_PORT = 1208;
    private static final int BROADCAST_ID = 255;

    private DatagramSocket sendSock;
    private Receiver receiver;

    public BATMAN(Receiver receiver, String dataDir)
    {
        this.receiver = receiver;

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
    public boolean start(String ip, String interfaceName)
    {
        if (Device.isAndroidSystem())
        {
            String result = Device.sysCommand("su -c \"" + Device.getDataDir()
                    + "/lib/batmand " + interfaceName + "\"");
            System.out.println(result);
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

        new Thread(new UDPReceiver()).start();
        return true;
    }

    @Override
    public boolean transmitData(String ip, String data)
    {

        return false;
    }

    @Override
    public boolean broadcastData(String subnet, String data)
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
        String result = Device.sysCommand("sudo killall batmand");

        sendSock.close();

        return true;
    }

    /**
     * Returns an array of BATMAN's current one-hop neighbor node numbers
     */
    private List<Integer> getCurrentNeighbors()
    {
        List<Integer> neighbors = new ArrayList<Integer>();

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

    private class UDPReceiver implements Runnable
    {
        private volatile boolean keepRunning = true;
        private DatagramSocket dgramSock;

        public UDPReceiver()
        {
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
        public void run()
        {
            while (keepRunning)
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
}
