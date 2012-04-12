package netbook.textmessenger;

public interface TextMessengerListener {
	
	public abstract void sendMessage(int number, String message);
	public abstract void sendMessageToAll(String message);
	public abstract void closeTextMessenger();
	public abstract void openTextMessenger(int nodeNum);
	

}
