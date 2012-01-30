package adhoc.aodv.exception;

public class DataExceedsMaxSizeException extends AodvException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataExceedsMaxSizeException(){
		
	}
	
	public DataExceedsMaxSizeException(String message){
		super(message);
	}

}
