package adhoc.aodv;

import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import adhoc.aodv.exception.InvalidNodeAddressException;
import adhoc.aodv.pdu.AodvPDU;
import adhoc.aodv.pdu.UserDataPacket;
import adhoc.etc.Debug;



/**
 * <pre>Note - Any observers should implement their update methods in the following way:
public void update(Observable o, Object arg) {
	MessageToObserver msg = (MessageToObserver)arg;
	int userPacketID, destination, type = msg.getMessageType();
	
	switch (type) {
	case ObserverConst.ROUTE_ESTABLISHMENT_FAILURE:
		//Note: any messages that had same destination has been removed from sending 
		int unreachableDestinationAddrerss  = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.DATA_RECEIVED:
		byte[] data = (byte[])msg.getContainedData();
		int senderAddress = (Integer)((PacketToObserver)msg).getSenderNodeAddress();
		...
		break;
	case ObserverConst.DATA_SENT_SUCCESS:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.INVALID_DESTINATION_ADDRESS:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.DATA_SIZE_EXCEEDES_MAX:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.ROUTE_INVALID:
		destination  = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.ROUTE_CREATED:
		destination = (Integer)msg.getContainedData();
		...
		break;
	default:
		break;
	}
}
 * </pre>
 * @author Rabie
 *
 */
public class Node extends Observable implements Runnable {
	private int nodeAddress;
	private int nodeSequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
	private int nodeBroadcastID = Constants.FIRST_BROADCAST_ID;
    private Sender sender;
    private Receiver receiver;
    private RouteTableManager routeTableManager;
    private Object sequenceNumberLock = 0;
    private Thread notifierThread;
    private Queue<MessageToObserver> messagesForObservers;
    private volatile boolean keepRunning = true;

	/**
	 * Creates an instance of the Node class
	 * @param nodeAddress
	 * @throws InvalidNodeAddressException Is thrown if the given node address is outside of the valid interval of node addresses
	 * @throws SocketException is cast if the node failed to instantiate port connections to the ad-hoc network
	 * @throws UnknownHostException
	 * @throws BindException this exception is thrown if network interface already is connected to a another network 
	 */
    public Node(int nodeAddress) throws InvalidNodeAddressException, SocketException, UnknownHostException, BindException {
    	if(nodeAddress > Constants.MAX_VALID_NODE_ADDRESS 
    			|| nodeAddress < Constants.MIN_VALID_NODE_ADDRESS){
    		//given address is out of the valid range
    		throw new InvalidNodeAddressException();
    	}
    	this.nodeAddress = nodeAddress;
    	routeTableManager = new RouteTableManager(nodeAddress, this);
        sender = new Sender(this, nodeAddress, routeTableManager);
        receiver = new Receiver(sender, nodeAddress, this, routeTableManager);
    	messagesForObservers = new ConcurrentLinkedQueue<MessageToObserver>();
    }
    
    /**
     * Starts executing the AODV routing protocol 
     * @throws UnknownHostException 
     * @throws SocketException 
     * @throws BindException 
     */
    public void startThread(){
    	keepRunning = true;
    	routeTableManager.startTimerThread();
		sender.startThread();
		receiver.startThread();
    	notifierThread = new Thread(this);
    	notifierThread.start();
    	Debug.print("Node: all library threads are running");
    }
    
    /**
     * Stops the AODV protocol. 
     * 
     * Note: using this method tells the running threads to terminate. 
     * This means that it does not insure that any remaining userpackets is sent before termination.
     * Such behavior can be achieved by monitoring the notifications by registering as an observer.
     */
    public void stopThread(){
    	keepRunning = false;
    	receiver.stopThread();
    	sender.stopThread();
    	routeTableManager.stopTimerThread();
    	notifierThread.interrupt();
    	Debug.print("Node: all library threads are stopped");
    }

    /**
     * Method to be used by the application layer to send data to a single destination node or all neighboring nodes (broadcast).
     * @param packetIdentifier is an ID that is associated for this packet. This is given from the application layer to identify which packet failed or succeed in sending
     * @param destinationAddress the address of the destination node. Should be set to Constants.BROADCAST_ADDRESS if the data is to be broadcasted. 
     * @param data an array of bytes containing the desired data to send. Note that the size of the data may not exceed Constants.MAX_PACKAGE_SIZE
     */
    public void sendData(int packetIdentifier, int destinationAddress, byte[] data){
    	sender.queueUserMessageFromNode(new UserDataPacket(packetIdentifier,destinationAddress, data, nodeAddress));
    }
	
    /**
     * Method for getting the current sequence number for this node
     * @return an integer value of the current sequence number
     */
	protected int getCurrentSequenceNumber(){
		return nodeSequenceNumber;
	}
	
	/**
	 * Increments the given number but does NOT set this number as the nodes sequence number
	 * @param number is the number which to increment
	 */
	protected int getNextSequenceNumber(int number){
		if((number >= Constants.MAX_SEQUENCE_NUMBER || number < Constants.FIRST_SEQUENCE_NUMBER)){
			return Constants.FIRST_SEQUENCE_NUMBER;
		} else {
			return number++;
		}
	}
	
	
	/**
	 * Increments and set the sequence number before returning the new value. 
	 * @return returns the next sequence number
	 */
	protected int getNextSequenceNumber(){
		synchronized (sequenceNumberLock) {
			if(nodeSequenceNumber == Constants.UNKNOWN_SEQUENCE_NUMBER
					|| nodeSequenceNumber == Constants.MAX_SEQUENCE_NUMBER	){
				
				nodeSequenceNumber = Constants.FIRST_SEQUENCE_NUMBER;
			}
			else{
				nodeSequenceNumber++;	
			}
			return nodeSequenceNumber;
		}
	}

	/**
	 * Increments the broadcast ID 
	 * @return returns the incremented broadcast ID
	 */
	protected int getNextBroadcastID() {
		synchronized (sequenceNumberLock) {
			if(nodeBroadcastID == Constants.MAX_BROADCAST_ID){
				nodeBroadcastID = Constants.FIRST_BROADCAST_ID;
			} else {
				nodeBroadcastID++;
			}
			return nodeBroadcastID;			
		}
	}
	
	/**
	 * Only used for debugging
	 * @return returns the current broadcast ID of this node
	 */
	protected int getCurrentBroadcastID(){
		return nodeBroadcastID;
	}
	
	/**
	 * Notifies the application layer about 
	 * @param senderNodeAddess the source node which sent a message
	 * @param data the actual data which the application message contained
	 */
	protected void notifyAboutDataReceived(int senderNodeAddess, byte[] data) {	
		 messagesForObservers.add(new PacketToObserver(senderNodeAddess,data,ObserverConst.DATA_RECEIVED));
		 wakeNotifierThread();
	}
	
	/**
	 * Notifies the observer(s) about the route establishment failure for a destination
	 * @param nodeAddress is the unreachable destination
	 */
	protected void notifyAboutRouteEstablishmentFailure(int faliedToReachAddress) {
		messagesForObservers.add(new ValueToObserver(faliedToReachAddress, ObserverConst.ROUTE_ESTABLISHMENT_FAILURE));
		wakeNotifierThread();
	}
	
	/**
	 * Notifies the observer(s) that a packet is sent successfully from this node.
	 * NOTE: This does not guarantee that the packet also is received at the destination node
	 * @param packetIdentifier the ID of a packet which the above layer can recognize
	 */
	protected void notifyAboutDataSentSucces(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.DATA_SENT_SUCCESS));
		wakeNotifierThread();
	}
	
	/**
	 * Notifies the observer(s) that an invalid destination address where detected for a user packet to be sent
	 * @param packetIdentifier an integer that identifies the user packet with bad destination address 
	 */
	protected void notifyAboutInvalidAddressGiven(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.INVALID_DESTINATION_ADDRESS));
		wakeNotifierThread();
	}
	
	protected void notifyAboutSizeLimitExceeded(int packetIdentifier){
		messagesForObservers.add(new ValueToObserver(packetIdentifier, ObserverConst.DATA_SIZE_EXCEEDES_MAX));
		wakeNotifierThread();
	}
	
	protected void notifyAboutRouteToDestIsInvalid(int destinationAddress){
		messagesForObservers.add(new ValueToObserver(destinationAddress, ObserverConst.ROUTE_INVALID));
		wakeNotifierThread();
	}
	
	protected void notifyAboutNewNodeReachable(int destinationAddress){
		messagesForObservers.add(new ValueToObserver(destinationAddress, ObserverConst.ROUTE_CREATED));
		wakeNotifierThread();
	}
	
	private void wakeNotifierThread(){
		synchronized (messagesForObservers) {
			messagesForObservers.notify();
		}
	}
	/**
	 * This interface defines the a structure for an observer to retrieve a message from the observable
	 * @author rabie
	 *
	 */
	public interface MessageToObserver{
		
		/**
		 * 
		 * @return returns the type of this message as a String
		 */
		public int getMessageType();
		
		/**
		 * This method is used to retrieve the data that the observable wants to notify about
		 * @return returns the object that is contained
		 */
		public Object getContainedData();
		
	}
	
	public class ValueToObserver implements MessageToObserver{
		private Integer value;
		private int type;
		
		public ValueToObserver(int value, int msgType) {
			this.value = new Integer(value);
			type = msgType;
		}
		@Override
		public Object getContainedData() {
			return value;
		}

		@Override
		public int getMessageType() {
			return type;
		}
		
	}
	
	/**
	 * This class presents a received package from another node, to the application layer
	 * @author Rabie
	 *
	 */
	public class PacketToObserver implements MessageToObserver{
		private byte[] data;
		private int senderNodeAddress;
		private int type;
		
		public PacketToObserver(int senderNodeAddress, byte[] data, int msgType) {
			type = msgType;
			this.data = data;
			this.senderNodeAddress = senderNodeAddress;
		}
		
		/**
		 * A method to retrieve the senders address of this data
		 * @return returns an integer value representing the unique address of the sending node
		 */
		public int getSenderNodeAddress(){
			return senderNodeAddress;
		}

		/**
		 * A method to retrieve the data sent
		 * @return returns a byte array containing the data which 
		 * where sent by another node with this node as destination
		 */
		@Override
		public Object getContainedData() {
			return data;
		}

		@Override
		public int getMessageType() {
			return type;
		}
	}

	protected void queuePDUmessage(AodvPDU pdu) {
		sender.queuePDUmessage(pdu);
	}

	@Override
	public void run() {
		while(keepRunning){
			try{
				synchronized (messagesForObservers) {
					while(messagesForObservers.isEmpty()){
						messagesForObservers.wait();
					}
				}
				setChanged();
				notifyObservers(messagesForObservers.poll());
			}catch (InterruptedException e) {
				// thread stopped
			}
		}
	}
}
