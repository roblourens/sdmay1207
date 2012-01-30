package adhoc.aodv.routes;

import java.util.HashMap;
import java.util.LinkedList;

import adhoc.aodv.exception.NoSuchRouteException;
import adhoc.etc.Debug;

public class RouteRequestTable {

	private HashMap<EntryKey, RouteRequestEntry> entries;
	private LinkedList<RouteRequestEntry> sortedEntries;
	private final Object tableLock = new Integer(0);
	
	public RouteRequestTable(){
		// contains known routes
		entries = new HashMap<EntryKey, RouteRequestEntry>();

		// containing the known routes, sorted such that the route with the
		// least 'aliveTimeLeft' is head
		sortedEntries = new LinkedList<RouteRequestEntry>();
	}
	
	
	public boolean routeRequestEntryExists(int sourceAddress, int broadcastID){
		return entries.containsKey(new EntryKey(sourceAddress, broadcastID));
	}
	
	/**
	 * Adds the given entry to the RREQ table
	 * @param rreqEntry the entry to be stored
	 * @param setTimer is set to false if the timer should not start count down the entry's time
	 * @return returns true if the route were added successfully. A successful add requires that no matching entry exists in the table
	 */
	public boolean addRouteRequestEntry(RouteRequestEntry rreqEntry, boolean setTimer){
		synchronized (tableLock) {
			EntryKey key = new EntryKey(rreqEntry.getSourceAddress(), rreqEntry.getBroadcastID());
			if(!entries.containsKey(key)){
				entries.put(key, rreqEntry);
				Debug.print(toString());
				if(setTimer){
					sortedEntries.addLast(rreqEntry);
				}
				return true;
			}
			return false;
		}
	}
	
	public void setRouteRequestTimer(int sourceAddres, int broadcastID) throws NoSuchRouteException{
		RouteRequestEntry rreqEntry = entries.get(new EntryKey(sourceAddres, broadcastID));
		if(rreqEntry != null){
			rreqEntry.resetAliveTimeLeft();
			synchronized (tableLock) {
				sortedEntries.addLast(rreqEntry);
			}
			return;
		}
		throw new NoSuchRouteException();
	}

	/**
	 * This method returns the desired RREQ entry
	 * OtherWise the method returns and remove the RREQ entry from the table
	 * @param sourceAddress the originator of the RREQ broadcast
	 * @param broadcastID the ID of the RREQ broadcast
	 * @param removeEntry is set to true if the table also should remove the entry in the table
	 * @return returns a RREQ entry
	 * @throws NoSuchRouteException Thrown if no table information is known about the entry
	 */
	public RouteEntry getRouteRequestEntry(int sourceAddress, int broadcastID, boolean removeEntry) throws NoSuchRouteException{
		synchronized (tableLock) {
			RouteRequestEntry entry = (RouteRequestEntry)entries.get(new EntryKey(sourceAddress, broadcastID));
			if (entry != null) {
				if(removeEntry){
					removeEntry(entry.getSourceAddress(), entry.getBroadcastID());
				}
				return entry;
			}
			throw new NoSuchRouteException();
		}
	}
	
	/**
	 * Removes an rreq entry from the table
	 * @param sourceAddress the node address of the originator
	 * @param broadcastID the broadcastID of the originator
	 * @return returns true if the entry existed and where removed successfully
	 */
	public boolean removeEntry(int sourceAddress, int broadcastID) {
		synchronized (tableLock) {
			RouteEntry rreqEntry = entries.remove(new EntryKey(sourceAddress,broadcastID));
			if (rreqEntry != null) {
				sortedEntries.remove(rreqEntry);
				Debug.print(toString());
				return true;
			}
			return false;
		}
	}
	
	public RouteEntry getNextRouteToExpire() throws NoSuchRouteException{
		RouteEntry route = sortedEntries.peek();
		if(route != null){
			return route;
		}
		throw new NoSuchRouteException();
	}
	
	public boolean isEmpty(){
		return sortedEntries.isEmpty();	
	}
	
	
	/**
	 * A route request is uniquely defined by the tuple (nodeAddress, broadcastID).
	 * This class is then used for generating a hashcode from this tuple so entries can be stored with an appropriate key
	 * @author Rabie
	 *
	 */
	private class EntryKey{
		private int nodeAddress;
		private int broadcastID;
		
		public EntryKey(int nodeAddress, int broadcastID){
			this.nodeAddress = nodeAddress;
			this.broadcastID = broadcastID;
		}
		
		@Override
		public boolean equals(Object obj) {
			EntryKey k = (EntryKey)obj;
			if(k.getNodeAddress() == nodeAddress && k.getBroadcastID() == broadcastID){
				return true;
			}
			return false;
		}
		
		@Override
		public int hashCode(){
			return (Integer.toString(nodeAddress)+";"+Integer.toString(broadcastID)).hashCode();
		}
		
		public int getNodeAddress(){
			return nodeAddress;
		}
		
		public int getBroadcastID(){
			return broadcastID;
		}
	}
	
	public String toString(){
		synchronized (tableLock) {
			if(entries.isEmpty()){
				return "RouteRequestTable is empty\n";
			}
			String returnString = "---------------------\n"+
								  "|Route Request Table:\n"+
								  "---------------------";
			for(RouteRequestEntry f :entries.values()){
				returnString += "\n"+"|Dest: "+f.getDestinationAddress()+" destSeqN: "+f.getDestinationSequenceNumber()+" src: "+f.getSourceAddress()+" broadID: "+f.getBroadcastID()+" retries left: "+f.getRetriesLeft()+" hopCount: "+f.getHopCount()+" TTL: "+(f.getAliveTimeLeft()-System.currentTimeMillis());

			}	
			return returnString+"\n---------------------\n";
		}
	}
}
