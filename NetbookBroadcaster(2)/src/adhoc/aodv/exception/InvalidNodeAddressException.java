package adhoc.aodv.exception;

public class InvalidNodeAddressException extends AodvException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidNodeAddressException() {
		
	}
	
	public InvalidNodeAddressException(String message){
		super(message);
	}
}
