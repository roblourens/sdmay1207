package android.TextMessenger.view;


import java.util.Observable;
import java.util.Observer;

import adhoc.aodv.Node;
import adhoc.aodv.Node.MessageToObserver;
import adhoc.aodv.Node.PacketToObserver;
import android.TextMessenger.view.R;
import android.TextMessenger.control.ButtonListner;
import android.TextMessenger.model.ClassConstants;
import android.TextMessenger.model.GPSManager;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;


public class Broadcast extends Activity implements Observer{
	
	int sequenceNumber;
	private Button send;
	private Button hello;
	private Button location;
	private ButtonListner listener;
	private Node node;
	private Handler handler;
	private EditText messageHistory;
	private GPSManager gpsManager;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.broadcast);
    
		listener = new ButtonListner(this);
		send = (Button) findViewById(R.id.sendButton);
		send.setOnClickListener(listener);
		hello = (Button) findViewById(R.id.helloButton);
		hello.setOnClickListener(listener);		
		location = (Button) findViewById(R.id.gpsButton);
		location.setOnClickListener(listener);		
		
		node = ClassConstants.getInstance().getNode();
		node.addObserver(this);
		
		LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		ClassConstants.getInstance().setLocationManager(locManager);
		gpsManager = new GPSManager();
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, gpsManager);
		
		messageHistory = (EditText) findViewById(R.id.receivedMsg);
		handler = new Handler(){
			public void handleMessage(Message msg) {
				displayMessage((String)msg.getData().getString("msg"));	
			}
		};
	}

	public void sendMessage(String type) {
		EditText number = (EditText) findViewById(R.id.nodeNumber);
		String destNum = number.getText().toString();
		int nodeNum = 0;
		
		if (destNum == "") {
			number.setText("Error");
			return;
		}
		
		try{
			nodeNum = Integer.parseInt(destNum);
		} catch (Exception e){
			e.printStackTrace();
			return;
		}
		String data = "";
		if(type.equals("hello")){
			data = "hello";
		} else {
			EditText text = (EditText) findViewById(R.id.msgText);
			data = text.getText().toString();
			text.setText("");
		}
		try{
			node.sendData(getNextSequenceNumber(), nodeNum, data.getBytes());
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void broadcastMessage(){
		node.sendData(getNextSequenceNumber(), 255, "CAST".getBytes());
	}
	
	public void sendGPS(){
		String location = gpsManager.getLocation();
		node.sendData(getNextSequenceNumber(), 255, location.getBytes());
	}
	
	private int getNextSequenceNumber() {
		return sequenceNumber++;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		String data = ""; 
		MessageToObserver msg = (MessageToObserver) arg1;
		
		int type = msg.getMessageType();
		switch(type) {
		case 0:	
			data = "Error: "+ msg.getContainedData().toString()+"\tMessage Type: "+msg.getMessageType();break;
		case 4:
			data = "Found Node: "+ msg.getContainedData().toString();break;
		case 3:
			data = "Lost Node: "+ msg.getContainedData().toString();break;
		case 2:
			data = "ACK Received";break;
		default:
			try{
				data = "From: "+((PacketToObserver) msg).getSenderNodeAddress()+"\n    Data: "+new String((byte[]) msg.getContainedData());break;
			} catch(Exception e){
				data = "From: Unknown  \n    Data: "+((Integer) msg.getContainedData()).toString();break;
			}
		}

		Message m = new Message();
		Bundle b = new Bundle();
		b.putString("msg", ">>> "+data+"\n");
		m.setData(b);
		handler.sendMessage(m);
	}
	
	private void displayMessage(String message){
		if (message != null) {								
			messageHistory.append(message);	
		}
	}

}
