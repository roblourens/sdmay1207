package adhoc.aodv;

import java.util.ArrayList;

import adhoc.aodv.exception.NoSuchRouteException;
import adhoc.aodv.exception.RouteNotValidException;
import adhoc.aodv.pdu.InternalMessage;
import adhoc.aodv.pdu.RERR;
import adhoc.aodv.pdu.RREQ;
import adhoc.aodv.routes.ForwardRouteEntry;
import adhoc.aodv.routes.ForwardRouteTable;
import adhoc.aodv.routes.RouteEntry;
import adhoc.aodv.routes.RouteRequestEntry;
import adhoc.aodv.routes.RouteRequestTable;

public class RouteTableManager {

	private volatile boolean keepRunning = true;
	private ForwardRouteTable forwardRouteTable;
	private RouteRequestTable routeRequestTable;
	private final Object tableLocks = new Integer(0);
	private TimeoutNotifier timeoutNotifier;
	private int nodeAddress;
	private Node parent;

	public RouteTableManager(int nodeAddress, Node parent) {
		this.nodeAddress = nodeAddress;
		this.parent = parent;
		forwardRouteTable = new ForwardRouteTable();
		routeRequestTable = new RouteRequestTable();
		timeoutNotifier = new TimeoutNotifier();
	}

	public void startTimerThread(){
		keepRunning = true;
		timeoutNotifier = new TimeoutNotifier();
		timeoutNotifier.start();	
	}
	
	public void stopTimerThread() {
		keepRunning = false;
		timeoutNotifier.stopThread();
	}

	
	/**
	 * Creates an entry and adds it to the appropriate table
	 * @param rreq The RREQ entry to be added
	 * @param setTimer is set to false if the timer should not start count down the entry's time
	 * @return returns true if the route were created and added successfully.
	 */
	protected boolean createRouteRequestEntry(RREQ rreq, boolean setTimer) {
		RouteRequestEntry entry;
		try {
			entry = new RouteRequestEntry(	rreq.getBroadcastId(),
											rreq.getSourceAddress(),
											rreq.getDestinationSequenceNumber(),
											rreq.getHopCount(), 
											rreq.getDestinationAddress());
		} catch (RouteNotValidException e) {
			return false;
		}
		
		if (routeRequestTable.addRouteRequestEntry(entry, setTimer)) {
			if(setTimer){
				//notify the timer since the RREQ table (the sorted list) isn't empty at this point
				synchronized (tableLocks) {
					tableLocks.notify();
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Creates an entry and adds it to the appropriate table
	 * @param destinationNodeAddress the destination address which this node will have a route for
	 * @param nextHopAddress is the neighbor address which to forward to if the destination should be reached 
	 * @param destinationSequenceNumber is the sequence number of the destination
	 * @param hopCount the number of intermediate node which will participate to forward a possible package for the destination
	 * @return returns true if the route were created and added successfully.
	 */
	protected boolean createForwardRouteEntry(int destinationNodeAddress, int nextHopAddress, int destinationSequenceNumber,
			int hopCount, boolean notifyObserver) {
		return createForwardRouteEntry(destinationNodeAddress, nextHopAddress, destinationSequenceNumber, hopCount, new ArrayList<Integer>(), notifyObserver);
	}

	/**
	 * Creates an entry and adds it to the appropriate table
	 * @param destinationNodeAddress the destination address which this node will have a route for
	 * @param nextHopAddress is the neighbor address which to forward to if the destination should be reached 
	 * @param destinationSequenceNumber is the sequence number of the destination
	 * @param hopCount the number of intermediate node which will participate to forward a possible package for the destination
	 * @param precursorNodes a list of node addresses which has used this route to forward packages
	 * @return returns true if the route were created and added successfully.
	 */
	protected boolean createForwardRouteEntry(int destinationNodeAddress, int nextHopAddress,
						int destinationSequenceNumber, int hopCount, ArrayList<Integer> precursorNodes, boolean notifyObserver) {
		ForwardRouteEntry forwardRouteEntry;
		try {
			forwardRouteEntry = new ForwardRouteEntry(	destinationNodeAddress,
														nextHopAddress,
														hopCount,
														destinationSequenceNumber, 
														precursorNodes	);
		} catch (RouteNotValidException e) {
			return false;
		}
		if (forwardRouteTable.addForwardRouteEntry(forwardRouteEntry)) {
			synchronized (tableLocks) {
				tableLocks.notify();
			}
			if(notifyObserver){
				parent.notifyAboutNewNodeReachable(destinationNodeAddress);
			}
			parent.queuePDUmessage(new InternalMessage(	Constants.FORWARD_ROUTE_CREATED,
														destinationNodeAddress)	);
			return true;
		}
		return false;
	}

	protected boolean routeRequestExists(int sourceAddress, int broadcastID) {
		return routeRequestTable.routeRequestEntryExists(sourceAddress, broadcastID);
	}

	/**
	 * method used to check the forward route table if a valid entry exist with a freshness that is as least as required
	 * @param destinationAddress the destination address of the node which a route is will be looked at
	 * @param destinationSequenceNumber specify any freshness requirement
	 * @return returns true if such a valid forward route exist with the seq number or higher 
	 */
	protected boolean validForwardRouteExists(int destinationAddress, int destinationSequenceNumber) {
		RouteEntry forwardRoute;
		try {
			forwardRoute = (ForwardRouteEntry) forwardRouteTable.getForwardRouteEntry(destinationAddress);
		} catch (NoSuchRouteException e) {
			return false;
		} catch (RouteNotValidException e) {
			return false;
		}

		if (forwardRoute.getDestinationSequenceNumber() >= destinationSequenceNumber) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param sourceAddress
	 * @param broadcastID
	 * @param removeEntry
	 * @return returns a RouteRequestEntry if any where found
	 * @throws NoSuchRouteException a NoSuchRouteException is cast in the event of an unsuccessful search
	 */
	protected RouteRequestEntry getRouteRequestEntry(int sourceAddress, int broadcastID, boolean removeEntry)
			throws NoSuchRouteException {

		return (RouteRequestEntry) routeRequestTable.getRouteRequestEntry(sourceAddress, broadcastID, removeEntry);
	}

	protected ForwardRouteEntry getForwardRouteEntry(int destinationAddress) throws NoSuchRouteException, RouteNotValidException {
		return forwardRouteTable.getForwardRouteEntry(destinationAddress);
	}

	protected void updateForwardRouteEntry(ForwardRouteEntry oldEntry, ForwardRouteEntry newEntry) throws NoSuchRouteException{
		if (Receiver.isIncomingRouteInfoBetter(	newEntry.getDestinationSequenceNumber(), oldEntry.getDestinationSequenceNumber(), 
												newEntry.getHopCount(),	oldEntry.getHopCount())) {
			if(forwardRouteTable.updateForwardRouteEntry(newEntry)){
				synchronized (tableLocks) {
					tableLocks.notify();
				}
			}
		}
	}
	
	protected boolean RemoveForwardRouteEntry(int destinationAddress) {
		return forwardRouteTable.removeEntry(destinationAddress);
	}

	protected int getLastKnownDestSeqNum(int destinationAddress) throws NoSuchRouteException {
		return forwardRouteTable.getLastKnownDestSeqNumber(destinationAddress);
	}
	
	protected ArrayList<Integer> getPrecursors(int destinaitonAdrress){
		return forwardRouteTable.getPrecursors(destinaitonAdrress);
	}

	/**
	 * Makes a forward route valid, updates it sequence number if necessary and resets the AliveTimeLeft
	 * @param destinationAddress used to determine which forward route to set valid
	 * @param newDestinationSeqNumber this destSeqNum is only set in the entry if it is greater that the existing destSeqNum
	 * @throws NoSuchRouteException thrown if no table information is known about the destination
	 */
	protected void setValid(int destinationAddress, int newDestinationSeqNumber) throws NoSuchRouteException {
		forwardRouteTable.setValid(destinationAddress, newDestinationSeqNumber,true);
	}
	
	protected void setInvalid(int destinationAddress, int newDestinationSeqNumber) throws NoSuchRouteException {
		forwardRouteTable.setValid(destinationAddress,newDestinationSeqNumber,false);
	}

	/**
	 * 
	 * @param destinationAddress
	 * @return
	 * @throws NoSuchRouteException thrown if no table information is known about the destination
	 * @throws RouteNotValidException thrown if a route were found, but is marked as invalid
	 */
	protected int getHopCount(int destinationAddress) throws NoSuchRouteException, RouteNotValidException {
		return ((ForwardRouteEntry) forwardRouteTable.getForwardRouteEntry(destinationAddress)).getHopCount();
	}

	/**
	 * resets the time left to live of the RREQ entry
	 * 
	 * @param sourceAddress
	 * @param brodcastID
	 * @throws NoSuchRouteException thrown if no table information is known about the destination
	 */
	protected void setRouteRequestTimer(int sourceAddress, int broadcastID) throws NoSuchRouteException {
		routeRequestTable.setRouteRequestTimer(sourceAddress, broadcastID);
		//wake the timer thread since a RREQ should be monitored
		synchronized (tableLocks) {
			tableLocks.notify();
		}
	}

	private class TimeoutNotifier extends Thread {
		public TimeoutNotifier() {
			super("TimeoutNotifier");
		}
		
		public void run() {
			while (keepRunning) {
				try {
					synchronized (tableLocks) {
						while (routeRequestTable.isEmpty() && forwardRouteTable.isEmpty()) {
							tableLocks.wait();
						}
					}
					long time = getMinimumTime() - System.currentTimeMillis();
					//Debug.print("Timer is sleeping for: "+time+" millSec");
					if (time > 0) {
						sleep(time);
					}
	
					try {
						// Route Request clean up
						RouteRequestEntry route = (RouteRequestEntry) routeRequestTable.getNextRouteToExpire();
						while (route.getAliveTimeLeft() <= System.currentTimeMillis()) {
							routeRequestTable.removeEntry(route.getSourceAddress(), route.getBroadcastID());
							//Debug.print(route.toString());
							if (route.getSourceAddress() == nodeAddress) {
								if (!validForwardRouteExists(route.getDestinationAddress(), route.getDestinationSequenceNumber())) {
									if (route.resend()) {
										//create a new RREQ message to broadcast
										RREQ newReq = new RREQ(nodeAddress,
																route.getDestinationAddress(),
																parent.getCurrentSequenceNumber(),
																route.getDestinationSequenceNumber(),
																parent.getNextBroadcastID());
										//update the RREQ entry
										route.setBroadcastID(newReq.getBroadcastId());
										//reinsert the entry with no timer
										routeRequestTable.addRouteRequestEntry(route, false);
										//let the sender broadcast the RREQ
										parent.queuePDUmessage(newReq);
									} else {
										// all RREQ retires is used. Notify the application layer
										parent.queuePDUmessage(new InternalMessage(Constants.RREQ_FAILURE_PDU, route.getDestinationAddress()));
										parent.notifyAboutRouteEstablishmentFailure(route.getDestinationAddress());
									}
								}
							}
							route = (RouteRequestEntry) routeRequestTable.getNextRouteToExpire();
						}
					} catch (NoSuchRouteException e) {
						// route request table is empty
					}
	
					// Forward Route Cleanup
					ForwardRouteEntry froute;
					try {
						froute = (ForwardRouteEntry) forwardRouteTable.getNextRouteToExpire();
	
						while (froute.getAliveTimeLeft() <= System.currentTimeMillis()) {
							
							//is froute a neighbour?
							if (froute.getHopCount() == 1 && froute.isValid()) {
								forwardRouteTable.toString();
								setInvalid(froute.getDestinationAddress(), froute.getDestinationSequenceNumber());
								parent.notifyAboutRouteToDestIsInvalid(froute.getDestinationAddress());
								
								for(RERR rerr :forwardRouteTable.findBrokenRoutes(froute.getDestinationAddress())){
									parent.queuePDUmessage(rerr);
								}
							}
							else if (froute.isValid()) {
								forwardRouteTable.setValid(froute.getDestinationAddress(), froute.getDestinationSequenceNumber(), false);
								parent.notifyAboutRouteToDestIsInvalid(froute.getDestinationAddress());
							} 
							else {
								forwardRouteTable.removeEntry(froute.getDestinationAddress());
							}
							froute = (ForwardRouteEntry) forwardRouteTable.getNextRouteToExpire();
						}
					} catch (NoSuchRouteException e1) {
						// ForwardRoute table is empty
					}
				} catch (InterruptedException e) {

				}
			}
		}

		private long getMinimumTime() {
			long a = Long.MAX_VALUE, b = Long.MAX_VALUE;
			try {
				a = routeRequestTable.getNextRouteToExpire().getAliveTimeLeft();
			} catch (NoSuchRouteException e) {

			}
			try {
				b = forwardRouteTable.getNextRouteToExpire().getAliveTimeLeft();
			} catch (NoSuchRouteException e2) {
				if(a == Long.MAX_VALUE){
					return -1;
				}
				return a;
			}
			return (a < b ? a : b);
		}

		public void stopThread() {
			this.interrupt();
		}
	}
}
