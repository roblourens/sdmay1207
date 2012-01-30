package adhoc.aodv.pdu;

import java.util.ArrayList;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.BadPduFormatException;

public class RERR extends AodvPDU {
	private int unreachableNodeAddress;
	private int unreachableNodeSequenceNumber;
	private ArrayList<Integer> destAddresses = new ArrayList<Integer>();

	
	
	public RERR(){
		
	}
	
	/**
	 * 
	 * @param unreachableNodeAddress
	 * @param unreachableNodeSequenceNumber
	 * @param destinationAddresses
	 */
    public RERR(int unreachableNodeAddress ,int unreachableNodeSequenceNumber, ArrayList<Integer> destinationAddresses) {
    	this.unreachableNodeAddress = unreachableNodeAddress;
    	this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
    	pduType = Constants.RERR_PDU;
        destAddresses = destinationAddresses;
        destAddress = -1;
    }

	/**
	 * Constructor of a route error message  
	 * @param
	 * @param
	 * @param destinationAddress the node which hopefully will receive this PDU packet
	 */
    public RERR(int unreachableNodeAddress ,int unreachableNodeSequenceNumber, int destinationAddress){
    	this.unreachableNodeAddress = unreachableNodeAddress;
    	this.unreachableNodeSequenceNumber = unreachableNodeSequenceNumber;
    	pduType = Constants.RERR_PDU;
        destAddress = destinationAddress;
    }
    
	public int getUnreachableNodeAddress(){
		return unreachableNodeAddress;
	}
	
	public int getUnreachableNodeSequenceNumber(){
		return unreachableNodeSequenceNumber;
	}
	
	public ArrayList<Integer> getAllDestAddresses(){
		return destAddresses;
	}
	
	@Override
	public byte[] toBytes() {
		return this.toString().getBytes();
	}
	
	@Override
	public String toString() {
		return Byte.toString(pduType)+";"+unreachableNodeAddress+";"+unreachableNodeSequenceNumber;
	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		String[] s = new String(rawPdu).split(";",3);
		if(s.length != 3){
			throw new BadPduFormatException(	"RERR: could not split " +
												"the expected # of arguments from rawPdu. " +
												"Expecteded 3 args but were given "+s.length	);
		}
		try {
			pduType = Byte.parseByte(s[0]);
			if(pduType != Constants.RERR_PDU){
				throw new BadPduFormatException(	"RERR: pdu type did not match. " +
													"Was expecting: "+Constants.RERR_PDU+
													" but parsed: "+pduType	);
			}
			unreachableNodeAddress = Integer.parseInt(s[1]);
			unreachableNodeSequenceNumber = Integer.parseInt(s[2]);
		} catch (NumberFormatException e) {
			throw new BadPduFormatException("RERR: falied in parsing arguments to the desired types");
		}	
	}
}
