package adhoc.aodv.pdu;

import adhoc.aodv.exception.BadPduFormatException;
import adhoc.etc.Debug;

public class InternalMessage extends AodvPDU{

	
	public InternalMessage(byte pduType, int destinationAddress){
		this.pduType = pduType;
		this.destAddress = destinationAddress;
	}
	
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		Debug.print("DO NOT USE");
		
	}

	@Override
	public byte[] toBytes() {
		Debug.print("DO NOT USE");
		return null;
	}

}
