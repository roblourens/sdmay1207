package adhoc.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import aodv.Constants;
import aodv.exception.DataExceedsMaxSizeException;

public class UdpSender {
	private DatagramSocket datagramSocket;
	private int receiverPort = 8888;
	private String subNet = "192.168.2.";
	
	public UdpSender() throws SocketException, UnknownHostException, BindException{
	    datagramSocket = new DatagramSocket(8881);
	}

	/**
	 * Sends data using the UDP protocol to a specific receiver
	 * @param destinationNodeID indicates the ID of the receiving node. Should be a positive integer.
	 * @param data is the message which is to be sent. 
	 * @throws IOException 
	 * @throws SizeLimitExceededException is thrown if the length of the data to be sent exceeds the limit
	 */
	public boolean sendPacket(int destinationNodeID, byte[] data) throws IOException, DataExceedsMaxSizeException{
		if(data.length <= Constants.MAX_PACKAGE_SIZE){
				InetAddress IPAddress = InetAddress.getByName(subNet+destinationNodeID);
				//do we have a packet to be broadcasted?
				DatagramPacket sendPacket;
				if(destinationNodeID == Constants.BROADCAST_ADDRESS){
					datagramSocket.setBroadcast(true);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort+1);
				}else {
					datagramSocket.setBroadcast(false);
					sendPacket = new DatagramPacket(data, data.length, IPAddress, receiverPort);
				}
				
				datagramSocket.send(sendPacket);
				return true;
			} else {
				throw new DataExceedsMaxSizeException();
			}
	}
	
	public void closeSoket(){
		datagramSocket.close();
	}

}
