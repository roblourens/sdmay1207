package adhoc.aodv.routes;


import java.util.ArrayList;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.RouteNotValidException;
import adhoc.etc.Debug;

public class ForwardRouteEntry extends RouteEntry {

    private ArrayList<Integer> precursorNodes = new ArrayList<Integer>();
    private volatile boolean isValid = true;
    private int nextHop;
    
    public ForwardRouteEntry(int destAddress, int nextHopAddress, int hopCount, int destSeqNum, ArrayList<Integer>  precursorNodes) throws RouteNotValidException {
    	super(hopCount, destSeqNum, destAddress);
    	if(nextHopAddress <= Constants.MAX_VALID_NODE_ADDRESS 
    			&& nextHopAddress >= Constants.MIN_VALID_NODE_ADDRESS
    			&& precursorNodes != null){
	    	this.nextHop = nextHopAddress;
	        for(int node : precursorNodes){
	        	addPrecursorAddress(node);
	        }
	        resetAliveTimeLeft();
	   	} else {
	   		throw new RouteNotValidException("RouteEntry: invalid parameters given");
	   	}
    }
    
    /**
     * Adds node as a precursor, so a RRER can be sent to this node in case of route failure
     * @param nodeAddress the address of the node which is using this forward route 
     * @return 
     */
    public boolean addPrecursorAddress(int nodeAddress){
        synchronized (precursorNodes) {
        	if(!precursorNodes.contains(nodeAddress)
        			&& nodeAddress <= Constants.MAX_VALID_NODE_ADDRESS
        			&& nodeAddress >= Constants.MIN_VALID_NODE_ADDRESS){
        		precursorNodes.add(nodeAddress);
        		return true;
        	}
        	return false;
		}
    }
    
    public ArrayList<Integer> getPrecursors(){
    	ArrayList<Integer> copy = new ArrayList<Integer>();
    	synchronized (precursorNodes) {
    		for(int address: precursorNodes){
    			copy.add(address);
    		}
		}
    	return copy;
    }
    
    public void resetAliveTimeLeft(){
    	synchronized (aliveTimeLock) {
    		alivetimeLeft = Constants.ROUTE_ALIVETIME + System.currentTimeMillis();	
		}
    }
    
    /**
     * @return returns true if this route is allowed to be used for packet forwarding.
     */
    public boolean isValid(){
    	return isValid;
    }
    
    public void setValid(boolean valid){
    	if(isValid != valid){
    		Debug.print("Forward Entry: isValid has changed to: "+valid);	
    	}
    	isValid = valid;
    }
    
    public boolean setSeqNum(int newSeqNr){
    	if(newSeqNr >= Constants.FIRST_SEQUENCE_NUMBER && newSeqNr <= Constants.MAX_SEQUENCE_NUMBER){
    		destSeqNum = newSeqNr;
    		return true;
    	}
    	return false;
    }
    
    public int getNextHop(){
    	return nextHop;
    }
}
