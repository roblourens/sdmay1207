package adhoc.aodv.exception;

public abstract class AodvException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AodvException(){}
	
	public AodvException(String message) {
		super(message);
	}

}
