package aoa.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import aoa.aodv.Receiver;


/**
 * Class running as a separate thread, and responsible for receiving data packets over the UDP protocol.
 * @author Rabie
 *
 */
public class UdpReceiver implements Runnable{
	private Receiver parent;
	private DatagramSocket datagramSocket;
	private UdpBroadcastReceiver udpBroadcastReceiver;
	private volatile boolean keepRunning = true;
	private Thread udpReceiverthread;
	
	public UdpReceiver(Receiver parent, int nodeAddress) throws SocketException, UnknownHostException, BindException{
		this.parent = parent;
		InetSocketAddress isa = new InetSocketAddress("192.168.2."+nodeAddress ,8888);
		datagramSocket = new DatagramSocket(isa);
		datagramSocket.setBroadcast(true);
		udpBroadcastReceiver = new UdpBroadcastReceiver(8888);
	}
	
	public void startThread(){
		keepRunning = true;
		udpBroadcastReceiver.startBroadcastReceiverthread();
		udpReceiverthread = new Thread(this);
		udpReceiverthread.start();
	}
	
	public void stopThread(){
		keepRunning = false;
		udpBroadcastReceiver.stopBroadcastThread();
		udpReceiverthread.interrupt();
	}
	
	public void run(){
		while(keepRunning){
			try {
				// 52kb buffer
				byte[] buffer = new byte[52000];
				DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);
	
				datagramSocket.receive(receivePacket);
			    byte[] result = new byte[receivePacket.getLength()];
			    System.arraycopy(receivePacket.getData(), 0, result, 0, receivePacket.getLength());
			    
			    String[] ip = receivePacket.getAddress().toString().split("\\.");
			    
			    int address = -1;
			    address = Integer.parseInt(ip[ip.length-1]);
			    parent.addMessage(address,result);
			} catch (IOException e) {

			}
		}
	}
	
	private class UdpBroadcastReceiver implements Runnable{
		private DatagramSocket brodcastDatagramSocket;
		private volatile boolean keepBroadcasting = true;
		private Thread udpBroadcastReceiverThread;
		
		public UdpBroadcastReceiver( int receiverPort) throws SocketException, BindException{
			brodcastDatagramSocket = new DatagramSocket(new InetSocketAddress("192.168.2.255",receiverPort+1));
		}
		
		public void startBroadcastReceiverthread(){
			keepBroadcasting = true;
			udpBroadcastReceiverThread = new Thread(this);
			udpBroadcastReceiverThread.start();
		}
		
		private void stopBroadcastThread(){
			keepBroadcasting = false;
			udpBroadcastReceiverThread.interrupt();
		}
		
		public void run(){
			while(keepBroadcasting){
				try {
					// 52kb buffer
					byte[] buffer = new byte[52000];
					DatagramPacket brodcastReceivePacket = new DatagramPacket(buffer,buffer.length);
	
					brodcastDatagramSocket.receive(brodcastReceivePacket);
	
				    byte[] result = new byte[brodcastReceivePacket.getLength()];
				    System.arraycopy(brodcastReceivePacket.getData(), 0, result, 0, brodcastReceivePacket.getLength());
				    
				    String[] ip = brodcastReceivePacket.getAddress().toString().split("\\.");
				    
				    int address = -1;
				    address = Integer.parseInt(ip[ip.length-1]);
				    parent.addMessage(address,result);
				} catch (IOException e) {
					
				}
			}
		}
	}
	
}
