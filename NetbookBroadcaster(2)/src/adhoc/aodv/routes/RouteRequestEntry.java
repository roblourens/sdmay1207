package adhoc.aodv.routes;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.RouteNotValidException;

public class RouteRequestEntry extends RouteEntry{
	//unique id for each route request
	private volatile int broadcastID;
	private int sourceAddress;
	//the number of route request retries 'RREQ' has left 
	private int retries = Constants.MAX_NUMBER_OF_RREQ_RETRIES;
	
	
/**
 * @param broadcastID unique id for this route request
 * @param sourceAddress
 * @param destinationSequenceNumber
 * @param hopCount
 * @throws RouteNotValidException 
 */
	public RouteRequestEntry(int broadcastID, int sourceAddress, int destinationSequenceNumber, int hopCount, int destinationAddress) throws RouteNotValidException{
		super(hopCount, destinationSequenceNumber,destinationAddress);
		if(sourceAddress <= Constants.MAX_VALID_NODE_ADDRESS 
				&& sourceAddress >= Constants.MIN_VALID_NODE_ADDRESS
				&& broadcastID <= Constants.MAX_BROADCAST_ID 
				&& broadcastID >= Constants.FIRST_BROADCAST_ID){
			this.sourceAddress = sourceAddress;
			this.broadcastID = broadcastID;
			resetAliveTimeLeft();
		} else {
			throw new RouteNotValidException("RouteEntry: invalid parameters given");
		}
	}

	public int getSourceAddress(){
		return sourceAddress;
	}
	
	public int getBroadcastID(){
		return broadcastID;
	}
	
	public void resetAliveTimeLeft(){
		synchronized (aliveTimeLock) {
			alivetimeLeft = System.currentTimeMillis() + Constants.PATH_DESCOVERY_TIME;	
		}
	}
	
	/**
	 * Method only used by the timer thread, to decrement the number of retries which this request is sent
	 * @return returns false if the request has been sent 3 times
	 */
	public boolean resend(){
		retries--;
		if(retries <= 0){
			return false;
		}
		return true;
	}
	
	/**
	 * Only used for debugging purposes!
	 * @return number of flood retries left
	 */
	public int getRetriesLeft(){
		return retries;
	}

	public boolean setBroadcastID(int broadcastID) {
		if(broadcastID <= Constants.MAX_BROADCAST_ID && broadcastID >= Constants.FIRST_BROADCAST_ID){
			this.broadcastID = broadcastID;
			return true;
		}
		else return false;
	}
	
	/**
	 * used for debugging
	 */
	public String toString(){
		return "SrcAdr:"+sourceAddress+" BroadID:"+broadcastID+" Retries:"+retries+" DestAdr:"+destAddress+" DestSeqNumb:"+destSeqNum;
	}
}
