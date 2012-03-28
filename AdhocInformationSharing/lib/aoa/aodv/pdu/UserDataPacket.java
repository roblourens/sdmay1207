package aoa.aodv.pdu;

import aoa.aodv.Constants;
import aoa.aodv.exception.BadPduFormatException;

public class UserDataPacket implements Packet{
	private byte[] data;
	private int destAddress;
	private byte pduType;
	private int sourceAddress;
	private int packetID;
	
	public UserDataPacket(){
		
	}
	
	public UserDataPacket(int packetIdentifier,int destinationAddress, byte[] data, int sourceAddress){
		pduType = Constants.USER_DATA_PACKET_PDU;
		packetID = packetIdentifier;
		destAddress = destinationAddress;
		this.data = data;
		this.sourceAddress = sourceAddress;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public int getSourceNodeAddress(){
		return sourceAddress;
	}
	
	@Override
    public int getDestinationAddress() {
        return destAddress;
    }
	
	@Override
	public byte[] toBytes() {
	    String header = pduType+";"+sourceAddress+";"+destAddress+";";
	    byte[] headerBytes = header.getBytes();
	    byte[] bytes = new byte[headerBytes.length+data.length];
	    System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
	    System.arraycopy(data, 0, bytes, headerBytes.length, data.length);
		
	    return bytes;
	}

	@Override
	public String toString(){
		return pduType+";"+sourceAddress+";"+destAddress+";"+new String(data);
	}
	
	@Override
	public void parseBytes(byte[] rawPdu) throws BadPduFormatException {
		String rawStr = new String(rawPdu);
		String[] s = rawStr.split(";",4);
		if(s.length != 4){
			throw new BadPduFormatException(	"UserDataPacket: could not split " +
												"the expected # of arguments from rawPdu. " +
												"Expecteded 4 args but were given "+s.length	);
		}
		try {
			pduType = Byte.parseByte(s[0]);
			if(pduType != Constants.USER_DATA_PACKET_PDU){
				throw new BadPduFormatException(	"UserDataPacket: pdu type did not match. " +
													"Was expecting: "+Constants.USER_DATA_PACKET_PDU+
													" but parsed: "+pduType	);
			}
			sourceAddress = Integer.parseInt(s[1]);
			destAddress = Integer.parseInt(s[2]);
			
			// find index of 3rd ;
			int found = 0;
			int i;
			for (i=0; found < 3; i++)
			{
			    if (rawStr.charAt(i) == ';')
			        found++;
			}
			data = new byte[rawPdu.length-i];
			System.arraycopy(rawPdu, i, data, 0, data.length);
		} catch (NumberFormatException e) {
			throw new BadPduFormatException(	"UserDataPacket: falied in parsing " +
												"arguments to the desired types"	);
		}
	}

	public int getPacketID() {
		return packetID;
	}

}