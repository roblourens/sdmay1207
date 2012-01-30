package adhoc.aodv.pdu;

import adhoc.aodv.Constants;
import adhoc.aodv.exception.BadPduFormatException;

public class HelloPacket implements Packet{
	private byte pduType;
	private int sourceAddress;
	private int sourceSeqNr;
	
	public HelloPacket(){
		
	}
	
	public HelloPacket(int sourceAddress, int sourceSeqNr){
		pduType = Constants.HELLO_PDU;
		this.sourceAddress = sourceAddress;
		this.sourceSeqNr = sourceSeqNr;
	}
	
	public int getSourceAddress(){
		return sourceAddress;
	}
	
	@Override
	public int getDestinationAddress() {
		//broadcast address
		return Constants.BROADCAST_ADDRESS;
	}
	
	public int getSourceSeqNr(){
		return sourceSeqNr;
	}

	@Override
	public byte[] toBytes() {
		return toString().getBytes();
	}
	
	@Override
	public String toString(){
		return pduType+";"+sourceAddress+";"+sourceSeqNr;
	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		String[] s = new String(rawPdu).split(";",3);
		if(s.length != 3){
			throw new BadPduFormatException(	"HelloPacket: could not split " +
												"the expected # of arguments from rawPdu. " +
												"Expecteded 3 args but were given "+s.length	);
		}
		try {
			pduType = Byte.parseByte(s[0]);
			if(pduType != Constants.HELLO_PDU){
				throw new BadPduFormatException(	"HelloPacket: pdu type did not match. " +
													"Was expecting: "+Constants.HELLO_PDU+
													" but parsed: "+pduType	);
			}
			sourceAddress = Integer.parseInt(s[1]);
			sourceSeqNr = Integer.parseInt(s[2]);
		} catch (NumberFormatException e) {
			throw new BadPduFormatException("HelloPacket: falied in parsing arguments to the desired types");
		}
	}

}
