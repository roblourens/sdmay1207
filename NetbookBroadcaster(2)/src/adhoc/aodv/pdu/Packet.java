package adhoc.aodv.pdu;

import adhoc.aodv.exception.BadPduFormatException;

public interface Packet {
		
	public byte[] toBytes();
	
	public String toString();
	
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException;

	public int getDestinationAddress();
}
