package android.TextMessenger.view;

import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;

import adhoc.aodv.Node;
import adhoc.aodv.exception.InvalidNodeAddressException;
import adhoc.etc.Debug;
import android.TextMessenger.view.R;
import android.TextMessenger.control.ButtonListner;
import android.TextMessenger.model.ClassConstants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class Connect extends Activity {
	private Button connect;
	private ButtonListner listener;
	private Node node;
	String ip;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);
		listener = new ButtonListner(this);
		connect = (Button) findViewById(R.id.connectButton);
		connect.setOnClickListener(listener);
			
	}
	
	
	public static native int runCommand(String command);

	static {
		System.loadLibrary("adhocsetup");
	}

	/**
	 * When connect is clicked, a ad-hoc network is startet
	 */
	public void clickConnect() {
		EditText number = (EditText) findViewById(R.id.nodeNumber);
		String myNum = number.getText().toString();
		if (myNum == "") {
			return;
		}
		try {
			ip = "192.168.2."+myNum;

			int result = Connect.runCommand("su -c \"/system/test.d/Bach_adhoc "+ip+"\"");
			Log.d("RESULTAT", ""+result);


			//Starting the routing protocol 
			node = new Node(Integer.parseInt(myNum)); 
			Debug.setDebugStream(System.out);

			ClassConstants classConstants = ClassConstants.getInstance();
			classConstants.setNode(node);
			node.startThread();
	
			Intent i = new Intent(this, Broadcast.class);
			startActivity(i);
			
		} catch (BindException e) {
			e.printStackTrace();
		} catch (InvalidNodeAddressException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		Log.d("DEBUG", "Node startet ");
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if(node != null){
			node.stopThread();
		}
		super.onDestroy();
	}

}