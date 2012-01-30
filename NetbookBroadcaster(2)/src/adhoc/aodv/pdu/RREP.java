package adhoc.aodv.pdu;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.BadPduFormatException;

public class RREP extends AodvPDU {
    private int hopCount = 0;
    private int srcSeqNum;

    
    public RREP(){
    }
    
    public RREP(	int sourceAddress,
    				int destinationAddress,
    				int sourceSequenceNumber,
    				int destinationSequenceNumber,
    				int hopCount){
    	
    	super(sourceAddress,destinationAddress,destinationSequenceNumber);
    	pduType = Constants.RREP_PDU;
    	srcSeqNum = sourceSequenceNumber;
    	this.hopCount = hopCount;
    }
    
    public RREP(	int sourceAddress,
    				int destinationAddress,
    				int sourceSequenceNumber,
    				int destinationSequenceNumber) {
    	
    	super(sourceAddress,destinationAddress,destinationSequenceNumber);
    	pduType = Constants.RREP_PDU;
    	srcSeqNum = sourceSequenceNumber;
    }
	
	public int getHopCount(){
		return hopCount;
	}
	
	public void incrementHopCount(){
		hopCount++;
	}
	
	public int getDestinationSequenceNumber(){
		return destSeqNum;
	}

	@Override
	public byte[] toBytes() {
		return this.toString().getBytes();
	}
	
	@Override
	public String toString() {
		return super.toString()+srcSeqNum+";"+hopCount;
	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		String[] s = new String(rawPdu).split(";",6);
		if(s.length != 6){
			throw new BadPduFormatException(	"RREP: could not split " +
												"the expected # of arguments from rawPdu. " +
												"Expecteded 6 args but were given "+s.length	);
		}
		try {
			pduType = Byte.parseByte(s[0]);
			if(pduType != Constants.RREP_PDU){
				throw new BadPduFormatException(	"RREP: pdu type did not match. " +
													"Was expecting: "+Constants.RREP_PDU+
													" but parsed: "+pduType	);
			}
			srcAddress = Integer.parseInt(s[1]);
			destAddress = Integer.parseInt(s[2]);
			destSeqNum =Integer.parseInt(s[3]);
			srcSeqNum = Integer.parseInt(s[4]);
			hopCount = Integer.parseInt(s[5]);
		} catch (NumberFormatException e) {
			throw new BadPduFormatException("RREP: falied in parsing arguments to the desired types");
		}
		
	}
}
