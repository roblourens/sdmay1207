package adhoc.aodv.routes;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.RouteNotValidException;

public abstract class RouteEntry {
    protected int destAddress;
    protected volatile long alivetimeLeft;
    protected volatile int destSeqNum;
    protected int hopCount;
    protected final Object aliveTimeLock = new Integer(0);
    
    public RouteEntry(int hopCount, int destSeqNum, int destAddress) throws RouteNotValidException{
    	if(destAddress <= Constants.MAX_VALID_NODE_ADDRESS && destAddress >= Constants.MIN_VALID_NODE_ADDRESS
        		&& (destSeqNum <= Constants.MAX_SEQUENCE_NUMBER 
        				&& destSeqNum >= Constants.FIRST_SEQUENCE_NUMBER 
        				|| destSeqNum == Constants.UNKNOWN_SEQUENCE_NUMBER)){
	    	this.hopCount = hopCount;
	    	this.destSeqNum = destSeqNum;
	    	this.destAddress = destAddress;
    	} else {
    		throw new RouteNotValidException("RouteEntry: invalid parameters given");
    	}
    }
    
    /**
     * 
     * @return the system time of when the route becomes stale
     */
    public long getAliveTimeLeft(){
    	synchronized (aliveTimeLock) {
    		return alivetimeLeft;
    	}
    }
    
    public abstract void resetAliveTimeLeft();
    
    public int getDestinationSequenceNumber(){
        return destSeqNum;	
    }
    
    public int getHopCount(){
    	return hopCount;
    }
    
    public int getDestinationAddress() {
        return destAddress;
    }
    
}
