package adhoc.etc;

import java.io.PrintStream;

public class Debug {
	static private PrintStream debugStream = null;
	
	static public void setDebugStream( PrintStream printstream){
		debugStream = printstream;
	}
		
	static public void print(String s){
		if(debugStream != null){
			debugStream.println(s);
		}
	}
}
