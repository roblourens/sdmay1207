package netbook;


import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import netbook.sensors.BatterySensor;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;
import sdmay1207.ais.network.model.TextMessage;

public class NetbookGUI extends JFrame implements ActionListener, Runnable, Observer{

	private static final long serialVersionUID = 1L;

	
	Thread runner;
	int nodeNum;
	Map<Integer, Node> nodes;
	NodeController nc;
	NodeView nodeView;
	MainPanel mainPanel;
	ConnectView conView;
	CameraView camView;
	JPanel views;
	
	final String NODEVIEW = "Node View";
	final String MAINVIEW = "Main View";
	final String CONVIEW  = "Connect View";
	final String CAMVIEW  = "Camnera View";
	
	final String DATADIR = "dataDir";
	String spdFile = DATADIR+"/sdmay1207.spd";
	boolean networkRunning;

	
	public NetbookGUI(){

		networkRunning = false;
						
		// Create the GUI Frame
		mainPanel = new MainPanel(this, nodeNum);
		nodeView = new NodeView(this);
		conView = new ConnectView(this);
		camView = new CameraView(this);
		
		views = new JPanel(new CardLayout());
		views.add(conView, CONVIEW);
		views.add(mainPanel, MAINVIEW);
		views.add(nodeView, NODEVIEW);
		views.add(camView, CAMVIEW);
		
		this.getContentPane().add(views);		
		this.setSize(800, 500);
		
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	System.out.println("Exiting");
		    	stopNetwork();
		    	System.exit(0); 
		    }
		});

	}
	
	
	
	



	public void createNetwork(){
		nc = new NodeController(nodeNum, DATADIR);
		
		nc.addNetworkObserver(this);
		nodes = nc.getNodesInNetwork();
		
		nc.addSensor(new BatterySensor());	
		startNetwork();
	}

	public void startNetwork() {
		nc.start(RoutingAlg.AODV);
		networkRunning = true;
	}
	
	public void stopNetwork() {
		nc.stop();
		networkRunning = false;
	}
	

	
	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource()== conView.connect){
			nodeNum = conView.getNodeNum();
			if(nodeNum >= 0){
				createNetwork();
				nodeView.createNodes(nc.getNodesInNetwork());
				mainPanel.updateNodeNum(nodeNum);
				((CardLayout) views.getLayout()).show(views, MAINVIEW);
				System.out.println("Connect Clicked");
			} 			
			
		} else if(action.getSource()== mainPanel.startStop){
			if(mainPanel.startStop.getText().equals("Start")){
				startNetwork();
				mainPanel.startStop.setText("Stop");
			} else {
				stopNetwork();
				mainPanel.startStop.setText("Start");
			}		
			System.out.println("Start/Stop Clicked");
			
		} else if(action.getSource() == mainPanel.nodeView){			// Node View
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			System.out.println("Node View Clicked");
			
		} else if(action.getSource() == mainPanel.camView){			// Cam View
			((CardLayout) views.getLayout()).show(views, CAMVIEW);
			System.out.println("Cam View Clicked");

				
		} else if(action.getSource() == mainPanel.nodeInfo){			// Node Info 
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			nodeView.openNode(nodeNum);			
			System.out.println("Node Info Clicked");
		
		} else if(action.getSource() == camView.playBtn){			// Play Button 
			if(new File(spdFile).exists()){
				System.out.println("File FOUND!");
				camView.play(spdFile);
			} else {
				System.out.println("File not found");
			}
			
		} else if(action.getSource() == nodeView.backBtn
				//|| action.getSource() == mapView.backBtn
				|| action.getSource() == camView.backBtn){
			((CardLayout) views.getLayout()).show(views, MAINVIEW);
			System.out.println("Back Button Clicked");
		}
	}

		
	@Override
    public void update(Observable observable, Object obj){
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event){
	        case RecvdHeartbeat:
	        case SentHeartbeat: 
	            Heartbeat hb = (Heartbeat) netEvent.data;
	            System.out.println("Got heartbeat from " + hb.from + ": "
	                    + hb.toString());
	            if(nodeView != null){
	            	nodeView.heartbeatUpdate(hb);
	            }
	            break;
	        	
	        case NodeJoined:
	            System.out.println("Node joined: " + netEvent.data);
	            mainPanel.displayMessage("Node joined: " + netEvent.data);
	            nodeView.addNode((Integer) netEvent.data);
	            break;
	            
	        case NodeLeft:
	            System.out.println("Node left: " + netEvent.data);
	            mainPanel.displayMessage("Node left: " + netEvent.data);
	            nodeView.removeNode((Integer) netEvent.data);
	            break;
	        
	        case RecvdTextMessage:
	        	System.out.println("Received TExt message");
	        	TextMessage tm = (TextMessage) netEvent.data;
	        	mainPanel.displayMessage(tm.from, nodeNum, tm.message);        	
	        	
        }
    }
	
	
	
	public void sendMessage(int nodeDest, String text) {
		TextMessage txtMsg = new TextMessage(text);
		//txtMsg.from = nodeNum;
		nc.sendNetworkMessage(txtMsg, nodeDest);	
	}
	
	public void sendMessageToAll(String text){
		TextMessage txtMsg = new TextMessage(text);
		//txtMsg.from = nodeNum;
		nc.broadcastNetworkMessage(txtMsg);
	}
	
	
	
	@Override
	public void run() {
	}
	
	public static void main(String[] args) {
		NetbookGUI gui = new NetbookGUI();
		//gui.start();
		gui.setVisible(true);
	}




	
}
