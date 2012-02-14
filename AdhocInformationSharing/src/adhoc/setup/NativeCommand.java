package adhoc.setup;

public class NativeCommand {
	
    public static native int runCommand(String command);
    
    static {
		System.loadLibrary("adhocsetup");
	}
	

}
