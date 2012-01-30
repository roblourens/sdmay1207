package adhoc.aodv.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import adhoc.aodv.Receiver;
import adhoc.aodv.exception.NoSuchRouteException;
import adhoc.aodv.exception.RouteNotValidException;
import adhoc.aodv.pdu.RERR;
import adhoc.etc.Debug;

public class ForwardRouteTable {

	private HashMap<Integer, ForwardRouteEntry> entries;
	private LinkedList<ForwardRouteEntry> sortedEntries;
	private final Object tableLock = new Integer(0);

	public ForwardRouteTable() {
		// contains known routes
		entries = new HashMap<Integer, ForwardRouteEntry>();

		// containing the known routes, sorted such that the route with the
		// least 'aliveTimeLeft' is head
		sortedEntries = new LinkedList<ForwardRouteEntry>();
	}

	/**
	 * Adds the given entry to the forwardRoute table
	 * @param forwardRouteEntry the entry to be stored
	 * @return returns true if the route were added successfully. A successful add requires that no matching entry exists in the table
	 */
	public boolean addForwardRouteEntry(ForwardRouteEntry forwardRouteEntry) {
		synchronized (tableLock) {
			if(!entries.containsKey(forwardRouteEntry.getDestinationAddress())) {
				entries.put(forwardRouteEntry.getDestinationAddress(), forwardRouteEntry);
				sortedEntries.addLast(forwardRouteEntry);
				Debug.print("ForwardRouteTable: Adding new forward route entry for dest: "+forwardRouteEntry.getDestinationAddress());
				Debug.print(this.toString());
				return true;
			}
			return false;
		}
	}
	
	/**
	 * 
	 * @param destAddress the destination address which to search for in the table
	 * @return returns false if the route does not exist
	 */
	public boolean removeEntry(int destAddress){
		synchronized (tableLock) {
			RouteEntry entry = entries.remove(destAddress);
			if (entry != null) {
				sortedEntries.remove(entry);
				Debug.print("ForwardRouteTable: removing forward route entry for dest: "+destAddress);
				Debug.print(this.toString());
				return true;
			}
			return false;
		}
	}
	
	public boolean updateForwardRouteEntry(ForwardRouteEntry entry) throws NoSuchRouteException{
		synchronized (tableLock) {
			if(removeEntry(entry.getDestinationAddress())
					&& addForwardRouteEntry(entry)){
				Debug.print("updateForwardRouteEntry: Updating route for dest: "+entry.getDestinationAddress() );
				return true;
			}
		}
		throw new NoSuchRouteException();
	}
	
	/**
	 * Method used to known the last known information about a routes 'freshness'
	 * @param destinationAddress the given destination which to search for in the table
	 * @return returns the destination sequence number of the forward entry
	 * @throws NoSuchRouteException is thrown if no such exists
	 */
	public int getLastKnownDestSeqNumber(int destinationAddress) throws NoSuchRouteException{
		RouteEntry entry = entries.get(destinationAddress);
		if(entry != null){
			return entry.getDestinationSequenceNumber();
		}
		throw new NoSuchRouteException();
	}
	
	public ArrayList<Integer> getPrecursors(int destinationAddress){
		synchronized (tableLock) {
			ForwardRouteEntry entry = entries.get(destinationAddress);
			if(entry != null){
				return entry.getPrecursors();
			}
			return new ArrayList<Integer>();
		}
	} 
	
	/**
	 * Makes a forward route valid, updates it sequence number if necessary and resets the AliveTimeLeft
	 * @param destinationAddress used to determine which forward route to set valid
	 * @param newDestinationSeqNumber this destSeqNum is only set in the entry if it is greater that the existing destSeqNum
	 * @throws NoSuchRouteException thrown if no table information is known about the destination
	 */
	public void setValid(int destinationAddress, int destinationSeqNumber, boolean validValue) throws NoSuchRouteException {
		ForwardRouteEntry entry = entries.get(destinationAddress);
		if(entry != null){
			entry.setValid(validValue);
			entry.resetAliveTimeLeft();
			synchronized (tableLock) {
				sortedEntries.remove(entry);
				sortedEntries.addLast(entry);
			}
			entry.setSeqNum(	Receiver.getMaximumSeqNum(	destinationSeqNumber,
															entry.getDestinationSequenceNumber()	)	);
			return;
		}
		throw new NoSuchRouteException();
	}

	/**
	 * 
	 * @param nodeAddress
	 * @return RouteEntry
	 * @throws NoSuchRouteException thrown if no table information is known about the destination
	 * @throws RouteNotValidException thrown if a route were found, but is marked as invalid
	 */
	public ForwardRouteEntry getForwardRouteEntry(int destinationAddress) throws NoSuchRouteException, RouteNotValidException {
		ForwardRouteEntry entry = entries.get(destinationAddress);
		if (entry != null) {
			entry.resetAliveTimeLeft();
			synchronized (tableLock) {	
				sortedEntries.remove(entry);
				sortedEntries.addLast(entry);
			}
			if (!(entry).isValid()) {
				throw new RouteNotValidException();
			}
			return entry;
		}
		throw new NoSuchRouteException();
	}
	
	/**
	 * Method for knowing if the table (sorted list) contain any entries
	 * @return true if the sortedlist is empty
	 */
	public boolean isEmpty(){
		return sortedEntries.isEmpty();	
	}
	
	/**
	 * 
	 * @return returns the route entry with the minimum time to live before expire  
	 * @throws NoSuchRouteException is thrown if no such exists
	 */
	public RouteEntry getNextRouteToExpire() throws NoSuchRouteException{
		RouteEntry route = null;
		route = sortedEntries.peek();	
		if(route != null){
			return route;
		}
		throw new NoSuchRouteException();
	}
	
	/**
	 * Searches the table for routes which match on the 'nextHopAddress'.
	 * The destination node of the matching entries is then used in a RERR pdu for later processing.
	 * The state of matching route entries is set to invalid
	 * @param brokenNodeAddress is the destination node which can not be reached any more
	 * @return ArrayList<RERR> returns an ArrayList of RERR messages
	 */
	public ArrayList<RERR> findBrokenRoutes(int brokenNodeAddress){
		ArrayList<RERR> brokenRoutes = new ArrayList<RERR>(); 
		LinkedList<ForwardRouteEntry> currentEntries = new LinkedList<ForwardRouteEntry>();
		synchronized (tableLock) {
			for(ForwardRouteEntry entry: sortedEntries){
				currentEntries.add(entry);
			}
			
			for(ForwardRouteEntry entry : currentEntries){
				if(entry.getNextHop() == brokenNodeAddress){
					RERR rerr = new RERR(entry.getDestinationAddress(), entry.getDestinationSequenceNumber(), entry.getPrecursors());
					brokenRoutes.add(rerr);
					try {
						setValid(entry.getDestinationAddress(), entry.getDestinationSequenceNumber(), false);
					} catch (NoSuchRouteException e) {
						Debug.print("RouteTableManager: NoSuchRouteException where thrown in findBrokenRoutes");
					}
				}
			}
		}
		return brokenRoutes;
	}
	
	/**
	 * only used for debugging
	 */
	public String toString(){
		synchronized (tableLock) {
			if(entries.size() != sortedEntries.size()){
				Debug.print("ForwardRouteTable: FATAL ERROR - inconsistensy in this table");
			}
			if(entries.isEmpty()){
				return "Forward Table is empty\n";
			}
			String returnString = "---------------------\n"+
								  "|Forward Route Table:\n"+
								  "---------------------";
			for(ForwardRouteEntry f :entries.values()){
				returnString += "\n"+"|Dest: "+f.getDestinationAddress()+" destSeqN: "+f.getDestinationSequenceNumber()+" nextHop: "+f.getNextHop()+" hopCount: "+f.getHopCount()+" isValid: "+f.isValid()+" TTL: "+(f.getAliveTimeLeft()-System.currentTimeMillis())+" precursors: ";
				for(int p  : f.getPrecursors()){
					returnString += p+" ";
				}
			}	
			return returnString+"\n---------------------\n";
		}
	}
}
