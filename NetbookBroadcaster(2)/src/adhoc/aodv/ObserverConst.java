package adhoc.aodv;

/**
 * Class defining all possible messages that an observer can react on.
 * 
 *<pre>Note - Any observers should implement their update methods in a similar way:
public void update(Observable o, Object arg) {
	MessageToObserver msg = (MessageToObserver)arg;
	int userPacketID, destination, type = msg.getMessageType();
	
	switch (type) {
	case ObserverConst.ROUTE_ESTABLISHMENT_FAILURE:
	//Note: any messages that had same destination has been removed from sending
		int unreachableDestinationAddrerss  = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.DATA_RECEIVED:
		byte[] data = (byte[])msg.getContainedData();
		int senderAddress = (Integer)((PacketToObserver)msg).getSenderNodeAddress();
		...
		break;
	case ObserverConst.DATA_SENT_SUCCESS:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.INVALID_DESTINATION_ADDRESS:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.DATA_SIZE_EXCEEDES_MAX:
		userPacketID = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.ROUTE_INVALID:
		destination  = (Integer)msg.getContainedData();
		...
		break;
	case ObserverConst.ROUTE_CREATED:
		destination = (Integer)msg.getContainedData();
		...
		break;
	default:
		break;
}
 * </pre>
 * @author rabie
 *
 */
public interface ObserverConst {
	
	public static final int ROUTE_ESTABLISHMENT_FAILURE = 0;
	
	public static final int DATA_RECEIVED = 1;
	
	public static final int DATA_SENT_SUCCESS = 2;
	
	public static final int ROUTE_INVALID = 3;
	
	public static final int ROUTE_CREATED = 4;
	
	public static final int INVALID_DESTINATION_ADDRESS = 5;
	
	public static final int DATA_SIZE_EXCEEDES_MAX = 6;

}
