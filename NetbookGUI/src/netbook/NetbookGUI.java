package netbook;


import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import netbook.sensors.BatterySensor;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;

public class NetbookGUI extends JFrame implements ActionListener, Runnable, Observer{

	private static final long serialVersionUID = 1L;

	
	Thread runner;
	int nodeNum;
	Map<Integer, Node> nodes;
	NodeController nc;
	NodeView nodeView;
	MainPanel mainPanel;
	MapView mapView;
	JPanel views;
	
	final String NODEVIEW = "Node View";
	final String MAINVIEW = "Main View";
	final String MAPVIEW  = "Map View";
	
	
	public NetbookGUI(){
		
		// Establish networking
		Random r = new Random();
		nodeNum = r.nextInt(245) + 10;
		
		final String dataDir = "dataDir";
		nc = new NodeController(nodeNum, dataDir);
		nc.addNetworkObserver(this);
		nodes = new ConcurrentHashMap<Integer, Node>();
		nodes.put(1, new Node(1));
		nodes.put(45, new Node(45));
		nodes.put(25, new Node(25));
		nodes.put(12, new Node(12));
		nodes.put(100, new Node(100));
		nodes.put(15, new Node(15));
		nodes.put(74, new Node(74));		
		nodes.put(13, new Node(13));
		nodes.put(102, new Node(102));
		nodes.put(82, new Node(82));
		nodes.put(73, new Node(73));
		nodes.put(nodeNum, new Node(nodeNum));
		
		nc.addSensor(new BatterySensor());
				
		// Create the GUI Frame
		mainPanel = new MainPanel(this, nodeNum, nodes);
		nodeView = new NodeView(this, nodes);
		mapView = new MapView(this);
		
		views = new JPanel(new CardLayout());
		views.add(mainPanel, MAINVIEW);
		views.add(nodeView, NODEVIEW);
		views.add(mapView, MAPVIEW);
		
		this.getContentPane().add(views);		
		this.setSize(800, 600);
		
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	System.out.println("Exiting");
		    	System.exit(0); 
		    }
		});

	}
	
	
	
	





	public void start() {
	   /* if ( runner == null ) {
	        runner = new Thread( this );
	        runner.start();
	    }*/
		nc.start(RoutingAlg.AODV);
	}
	
	public void stop() {
		//if ( runner != null && runner.isAlive()) runner.interrupt();
	    //runner = null;
		nc.stop();
	}
	
	@Override
	public void run() {
		while(runner != null){
			
		}
		
	}

	
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource()== mainPanel.startStop){
			if(mainPanel.startStop.getText().equals("Start")){
				start();
				mainPanel.startStop.setText("Stop");
			} else {
				stop();
				mainPanel.startStop.setText("Start");
			}		
			System.out.println("Start/Stop Clicked");
			
		} else if(action.getSource() == mainPanel.mapView){
			((CardLayout) views.getLayout()).show(views, MAPVIEW);
			System.out.println("Map View Clicked");
			
		} else if(action.getSource() == mainPanel.nodeView){
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			System.out.println("Node View Clicked");
			
		} else if(action.getSource() == mainPanel.nodeInfo){
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			openNode(nodeNum);			
			System.out.println("Node Info Clicked");
		
		} else if(action.getSource() == nodeView.backBtn){
			((CardLayout) views.getLayout()).show(views, MAINVIEW);
			System.out.println("Back Button Clicked from Node View");
			
		} else if(action.getSource() == mapView.backBtn){
			((CardLayout) views.getLayout()).show(views, MAINVIEW);
			System.out.println("Back Button Clicked from Map View");
		}
	}

	
	
	public void openNode(int nodeNumber){
		//Heartbeat hb = nc.getNodesInNetwork().get(nodeNum).getHeartbeat();
		//nodeView.openNode(hb, nodeNum);
		nodeView.openNode(null, nodeNumber);
	}
	
	@Override
    public void update(Observable observable, Object obj){
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event){
	        case RecvdHeartbeat:
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
	            break;
	        case NodeLeft:
	            System.out.println("Node left: " + netEvent.data);
	            mainPanel.displayMessage("Node left: " + netEvent.data);
	            break;
	            
	        case RevcdTextMessage:
        }
    }
	
	
	
	
	
	public static void main(String[] args) {
		NetbookGUI gui = new NetbookGUI();
		gui.start();
		gui.setVisible(true);
	}


	
}
