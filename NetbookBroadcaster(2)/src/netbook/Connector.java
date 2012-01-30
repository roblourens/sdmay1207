package netbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;

import adhoc.aodv.Node;
import adhoc.aodv.Node.PacketToObserver;
import adhoc.aodv.Node.ValueToObserver;
import adhoc.aodv.ObserverConst;

public class Connector implements Observer {
	
	private Node node;
	String ip;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    if (args.length < 2)
	        throw new RuntimeException("Need to provide last part of ip");
	    
		new Connector().connect(args[1]);
	}
	
	
	/**
	 * When connect is clicked, a ad-hoc network is startet
	 */
	public void connect(String myNum) {
		if (myNum == "") {
			return;
		}
		try {
			ip = "192.168.2."+myNum;
			
			//Starting the routing protocol
			node = new Node(Integer.parseInt(myNum));
			node.addObserver(this);
			//chatManager = new ChatManager(myNum, Integer.parseInt(myNum), node);
			node.startThread();
			getUserInput(node);
			node.stopThread();
		} catch (Exception e){
			System.out.print("FOUND EXCEPTION");
			e.printStackTrace();
		}
	}
	
	private void getUserInput(Node node){
		String input = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean end = false;
		
		while(!end){
		      System.out.println("Please enter command: ");
		      try {
		          input = br.readLine();
		      } catch (IOException ioe) {
		         System.out.println("IO error trying to get command!");
		         System.exit(1);
		      }
		      if(!input.equals(null)){
			      if(input.equals("help")){
			    	  System.out.println("COMMANDS\n\thelp\n\tid:msg\n\tid! - Hello Message\n\texit\n");
			      } else if(input.contains(":")){
			    	  int marker = input.indexOf(":");
			    	  String destID = input.substring(0, marker);
			    	  int destNode = Integer.parseInt(destID);
			    	  String msg = input.substring(marker+1);
			    	  node.sendData(13, destNode, msg.getBytes());
			      } else if(input.contains("!")){
			    	  String destID = input.substring(0, input.indexOf("!"));
			    	  int destNode = Integer.parseInt(destID);
			    	  String msg = "hello";
			    	  node.sendData(13, destNode, msg.getBytes());
		          }else if(input.equals("exit")){
			    	  end = true;
			      } else {
			    	  System.out.println("Error processing command\nNot Found\n");
			      }
		
		      }
		}
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		try{
			PacketToObserver msg = (PacketToObserver) arg1;
			int type = msg.getMessageType();
			int destination = msg.getSenderNodeAddress();
			switch (type) {
			case ObserverConst.DATA_RECEIVED: 
				System.out.println(">>> Received Message from "+destination+": \""+new String((byte[]) msg.getContainedData())+"\"");
				break;
			default:
				break;
			}
		} catch (Exception e){
			ValueToObserver msg = (ValueToObserver) arg1;
			int type = msg.getMessageType();
			switch(type) {
			case 0:	
				System.out.println(">>> Error: "+((Integer) msg.getContainedData()).toString()+"\tMessage Type: "+msg.getMessageType());break;
			case 4:
				System.out.println(">>> Found Node: "+((Integer) msg.getContainedData()).toString());break;
			case 3:
				System.out.println(">>> Lost Node: "+((Integer) msg.getContainedData()).toString());break;
			case 2:
				System.out.println(">>> ACK Received");break;
			default:
				System.out.println(">>> Value Response: "+((Integer) msg.getContainedData()).toString()+"\tMessage Type: "+msg.getMessageType());break;
			}
		}
		System.out.println("Please enter command: ");
	}
}