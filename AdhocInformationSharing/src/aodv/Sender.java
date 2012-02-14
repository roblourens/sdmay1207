package aodv;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import adhoc.etc.Debug;
import adhoc.udp.UdpSender;
import aodv.exception.AodvException;
import aodv.exception.DataExceedsMaxSizeException;
import aodv.exception.InvalidNodeAddressException;
import aodv.exception.NoSuchRouteException;
import aodv.pdu.AodvPDU;
import aodv.pdu.HelloPacket;
import aodv.pdu.Packet;
import aodv.pdu.RERR;
import aodv.pdu.RREQ;
import aodv.pdu.UserDataPacket;

public class Sender implements Runnable{
	private Node parent;
    private int nodeAddress;
    private NeighbourBroadcaster neighborBroadcaster;
    private Queue<Packet> pduMessages;
    private Queue<UserDataPacket> userMessagesToForward;
    private Queue<UserDataPacket> userMessagesFromNode;
    private final Object queueLock = new Integer(0);
    private RouteTableManager routeTableManager;
    private UdpSender udpSender;
    private boolean isRREQsent = false;
    private volatile boolean keepRunning = true;
    private Thread senderThread;
    
    public Sender(Node parent,int nodeAddress, RouteTableManager routeTableManager) throws SocketException, UnknownHostException, BindException {
    	this.parent = parent;
        this.nodeAddress = nodeAddress;
        neighborBroadcaster = new NeighbourBroadcaster();
		udpSender = new UdpSender();
        pduMessages = new ConcurrentLinkedQueue<Packet>();
        userMessagesToForward = new ConcurrentLinkedQueue<UserDataPacket>();
        userMessagesFromNode = new ConcurrentLinkedQueue<UserDataPacket>();
        this.routeTableManager = routeTableManager;
        
    }
    
    public void startThread(){
    	keepRunning = true;
    	neighborBroadcaster = new NeighbourBroadcaster();
    	neighborBroadcaster.start();
    	senderThread = new Thread(this);
    	senderThread.start();
    }
    
    public void stopThread(){
    	keepRunning = false;
    	neighborBroadcaster.stopBroadcastThread();
    	senderThread.interrupt();
    }
    
    public void run(){
    	while(keepRunning){
        	try {
	        	synchronized(queueLock){
	    			while(pduMessages.isEmpty() && userMessagesToForward.isEmpty() && (isRREQsent || userMessagesFromNode.isEmpty())){
	    				queueLock.wait();
	    			}
	    		}
	    		
	        	//Handle user data messages that is to be sent from this node
	        	if(!isRREQsent){
		        	if(!userMessagesFromNode.isEmpty()){
			    		UserDataPacket userData = userMessagesFromNode.peek();
			    		while(userData != null){
			    			try{
								if(!sendUserDataPacket(userData)){
									isRREQsent = true;
									//do not process any user messages before the head is sent
									break;
								} else {
									parent.notifyAboutDataSentSucces(userData.getPacketID());
								}
							} catch (DataExceedsMaxSizeException e) {
								parent.notifyAboutSizeLimitExceeded(userData.getPacketID());
			    			} catch (InvalidNodeAddressException e) {
			    				parent.notifyAboutInvalidAddressGiven(userData.getPacketID());
							}
			    			//it is expected that the queue still has the same userDataHeader object as head
				    		userMessagesFromNode.poll();
			    			userData = userMessagesFromNode.peek();
			    		}
		        	}
	        	}
	        	
	        	//Handles messages user data messages (received by other nodes) that are to be forwarded
	    		UserDataPacket userData = userMessagesToForward.peek();
	    		while(userData != null){
	    			try{
			    		if(!sendUserDataPacket(userData)){
			    			//do not process any user messages before the head is sent
			    			break;
			    		}
	    			} catch (InvalidNodeAddressException e) {
						Debug.print(e.getStackTrace().toString());
					} catch (DataExceedsMaxSizeException e) {
						Debug.print(e.getStackTrace().toString());
					}
	    			//it is expected that the queue still has the same userDataHeader object as head
	    			userMessagesToForward.poll();
	    			userData = userMessagesToForward.peek();
	    		}
	    		
	    		// Handle protocol messages
	    		Packet packet = pduMessages.poll();
	    		while(packet != null){
	    			if(packet instanceof AodvPDU){
	    				AodvPDU pdu = (AodvPDU)packet;
	    				try {
							handleAodvPDU(pdu);
						} catch (InvalidNodeAddressException e) {
							Debug.print(e.getMessage());
						} catch (DataExceedsMaxSizeException e) {
							Debug.print("FATAL ERROR: Aodv packet could not be sent because data size exceeded limit");
						}
	    			} else if(packet instanceof HelloPacket){
	    				try {
							broadcastPacket(packet);
							Debug.print("Sender: broadcasting hello message");
						} catch (DataExceedsMaxSizeException e) {
							Debug.print(e.getStackTrace().toString());
						}
	    			} else {
						Debug.print("Sender queue contained an unknown message Packet PDU!");
	    			}
		    		packet = pduMessages.poll();
	    		}
    		} catch (InterruptedException e) {

    		}
    	}    	
    }
    
    
    private void handleAodvPDU(AodvPDU pdu) throws InvalidNodeAddressException, DataExceedsMaxSizeException{
		switch (pdu.getType()) {
			case Constants.RREQ_PDU:
				broadcastPacket(pdu);
				if(pdu.getSourceAddress() == nodeAddress){
					try {
						routeTableManager.setRouteRequestTimer(	((RREQ)pdu).getSourceAddress(),
																((RREQ)pdu).getBroadcastId()	);
					} catch (NoSuchRouteException e) {
						Debug.print(e.getStackTrace().toString());
					}
				}
				break;
				
			case Constants.RREP_PDU:						
				if(!sendAodvPacket(pdu,pdu.getSourceAddress())){
					Debug.print("Sender: Did not have a forward route for sending back the RREP message to: "+pdu.getSourceAddress()+" the requested destination is: "+pdu.getDestinationAddress());
				}
				break;
				
			case Constants.RERR_PDU:
				RERR rerr = (RERR)pdu;
				for(int nodeAddress: rerr.getAllDestAddresses()){
					if(!sendAodvPacket(new RERR(	rerr.getUnreachableNodeAddress(),
											rerr.getUnreachableNodeSequenceNumber(),
											nodeAddress	), nodeAddress)){
						Debug.print("Sender: Did not have a forward route for sending the RERR message!!");
					}
				}
				break;
				
			case Constants.RREQ_FAILURE_PDU:
				cleanUserDataPacketsFromNode(pdu.getDestinationAddress());
				isRREQsent = false;
				synchronized (queueLock) {
					queueLock.notify();
				}
				break;
				
			case Constants.FORWARD_ROUTE_CREATED:
				UserDataPacket userPacket = userMessagesFromNode.peek();
				if(userPacket != null && pdu.getDestinationAddress() == userPacket.getDestinationAddress()){
					isRREQsent = false;
					synchronized (queueLock) {
						queueLock.notify();
					}
				}
				break;
				
			default:
				Debug.print("Sender queue contained an unknown message AODV PDU!");
				break;
		}
    }
    /**
     * 
     * @param packet is the message which are to be broadcasted to the neighboring nodes
     * @throws SizeLimitExceededException 
     */
	private boolean broadcastPacket(Packet packet) throws DataExceedsMaxSizeException {
			try {
				return udpSender.sendPacket(Constants.BROADCAST_ADDRESS, packet.toBytes());
			} catch (IOException e) {
				Debug.print(e.getStackTrace().toString());
				return false;
			}
	}
	
	
	private boolean sendUserDataPacket(UserDataPacket packet) throws DataExceedsMaxSizeException, InvalidNodeAddressException{
		if(		packet.getDestinationAddress() != Constants.BROADCAST_ADDRESS
				&& packet.getDestinationAddress() >= Constants.MIN_VALID_NODE_ADDRESS
				&& packet.getDestinationAddress() <= Constants.MAX_VALID_NODE_ADDRESS){
			if(packet.getDestinationAddress() == nodeAddress){
				throw new InvalidNodeAddressException("Sender: It is not allowed to send to our own address: "+nodeAddress);
			}
			try {
				int nextHop = routeTableManager.getForwardRouteEntry(packet.getDestinationAddress()).getNextHop();
				try {
					return udpSender.sendPacket(nextHop, packet.toBytes());
				} catch (IOException e) {
					Debug.print(e.getStackTrace().toString());
					return false;
				}
			}catch (DataExceedsMaxSizeException e){
				throw new DataExceedsMaxSizeException();
			} catch (AodvException e) {
				//Discover the route to the desired destination
				//if a route to the destination isn't request before
				try {
					int lastKnownDestSeqNum = routeTableManager.getLastKnownDestSeqNum(packet.getDestinationAddress());
					if(packet.getSourceNodeAddress() == nodeAddress){
						//Discover the route to the desired destination
						//if a route to the destination isn't request before
						if(!createNewRREQ(packet.getDestinationAddress(), lastKnownDestSeqNum, false)){
							Debug.print("Sender: Failed to add new RREQ entry to the request table. Src: "+nodeAddress+" broadID: "+parent.getCurrentBroadcastID());
							return false;
						}
					} else {
						queuePDUmessage(new RERR(	packet.getDestinationAddress(), 
													lastKnownDestSeqNum,
													packet.getSourceNodeAddress()	)	);
						cleanUserDataPacketsToForward(packet.getDestinationAddress());
					}
				} catch (NoSuchRouteException e1) {
					if(packet.getSourceNodeAddress() == nodeAddress){
						//Discover the route to the desired destination
						//if a route to the destination isn't request before
						createNewRREQ(packet.getDestinationAddress(), Constants.UNKNOWN_SEQUENCE_NUMBER, false);
					} else {
						queuePDUmessage(new RERR(	packet.getDestinationAddress(), 
													Constants.UNKNOWN_SEQUENCE_NUMBER,
													packet.getSourceNodeAddress()	)	);
						cleanUserDataPacketsToForward(packet.getDestinationAddress());
					}
				}
				return false;
			}
		} else if( packet.getDestinationAddress() == Constants.BROADCAST_ADDRESS){
			return broadcastPacket(packet);
		} else {
			 throw new InvalidNodeAddressException("Sender: got request to send a user packet which had  an invalid node address: "+packet.getDestinationAddress());
		}
	}
    
	/**
	 * Note: this method is able to send messages to itself if necessary. Note: DO NOT USE FOR BROADCASTING
	 * @param destinationNodeAddress should not be exchanged as the 'nextHopAddress'. DestinationNodeAddress is the final place for this packet to reach
	 * @param packet is the message to be sent
	 * @return false if no route to the desired destination is currently known.
	 * @throws InvalidNodeAddressException 
	 * @throws SizeLimitExceededException 
	 */
    private boolean sendAodvPacket(AodvPDU packet, int destinationNodeAddress) throws InvalidNodeAddressException{
    	if(destinationNodeAddress >= Constants.MIN_VALID_NODE_ADDRESS 
    			&& destinationNodeAddress <= Constants.MAX_VALID_NODE_ADDRESS){
			try {
				int nextHop = routeTableManager.getForwardRouteEntry(destinationNodeAddress).getNextHop();
		    		return udpSender.sendPacket(nextHop, packet.toBytes());
			} catch (IOException e) {
				Debug.print("Sender: IOExeption when trying to send a packet to: "+destinationNodeAddress);
				return false;
			} catch (AodvException e){
				return false;
			}
    	} else {
    		throw new InvalidNodeAddressException("Sender: Tried to send an AODV packet but the destination address is out valid range");
    	}
    }
    
    /**
     * 
     * Creates and queues a new RREQ
     * @param destinationNodeAddress is the destination that you want to discover a route to
     * @param lastKnownDestSeqNum
     * @param setTimer is set to false if the timer should not start count down the entry's time
     * @return returns true if the route were created and added successfully.
     */
    private boolean createNewRREQ(int destinationNodeAddress, int lastKnownDestSeqNum, boolean setTimer){
    	RREQ rreq = new RREQ(	nodeAddress,
				destinationNodeAddress,
				parent.getNextSequenceNumber(),
				lastKnownDestSeqNum,
				parent.getNextBroadcastID()	);
    	if(routeTableManager.createRouteRequestEntry(rreq,setTimer)){
    		queuePDUmessage(rreq);
    		return true;
    	}
    	return false;
    }
    
    /**
     * Method for queuing protocol messages for sending
     * @param aodvPDU is the Protocol Data Unit to be queued. 
     */
    protected void queuePDUmessage(AodvPDU aodvPDU){
    	pduMessages.add(aodvPDU);
    	synchronized (queueLock) {
    		queueLock.notify();	
		}
    }
    
    private void queueHelloPacket(Packet helloPacket){
    	pduMessages.add(helloPacket);
    	synchronized (queueLock) {
    		queueLock.notify();
		}
    }
    

    protected void queueUserMessageToForward(UserDataPacket userData){
    	userMessagesToForward.add(userData);
    	synchronized (queueLock) {
			queueLock.notify();
		}
    }
    
    protected void queueUserMessageFromNode(UserDataPacket userPacket){
    	userMessagesFromNode.add(userPacket);
    	synchronized (queueLock) {
    		queueLock.notify();
		}
    }
    
    
    private void cleanUserDataPacketsToForward(int destinationAddress){
    	synchronized (userMessagesToForward) {
			for(UserDataPacket msg: userMessagesToForward){
				if(msg.getDestinationAddress() == destinationAddress){
					userMessagesToForward.remove(msg);
				}
			}
		}
    }
    
    /**
     * Removes every message from the user packet queue that matches the given destination
     * @param destinationAddress the destination which to look for
     */
    private void cleanUserDataPacketsFromNode(int destinationAddress){
    	synchronized (userMessagesFromNode) {
			for(UserDataPacket msg: userMessagesFromNode){
				if(msg.getDestinationAddress() == destinationAddress){
					userMessagesFromNode.remove(msg);
				}
			}
		}
    }
    
    
    private class NeighbourBroadcaster extends Thread {
    	private volatile boolean keepBroadcasting = true;

    	public NeighbourBroadcaster() {
			super("NeighbourBroadcaster");
		}
    	
    	public void stopBroadcastThread(){
    		keepBroadcasting = false;
    		this.interrupt();
    	}
    	
    	public void run(){
			while(keepBroadcasting){
				try {
    				sleep(Constants.BROADCAST_INTERVAL);
    				queueHelloPacket(new HelloPacket(nodeAddress,parent.getCurrentSequenceNumber()));
	    		} catch (InterruptedException e) {
	    			
	    		}
			}
    	}
    }
}
