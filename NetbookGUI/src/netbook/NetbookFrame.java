package netbook;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import netbook.node.Node;
import netbook.sensors.BatterySensor;
import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.NetworkInterface.RoutingAlg;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.TextMessage;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.NetbookGPS;
import sdmay1207.cc.Point2PointCommander;
import sdmay1207.networkrejoining.NetworkRejoinMonitor.NetworkRejoinListener;

public class NetbookFrame extends JFrame implements ActionListener, Runnable,
		Observer, NetworkRejoinListener {

	private static final long serialVersionUID = 1L;

	Thread runner;
	int nodeNum;
	Map<Integer, Node> nodes;
	NodeController nc;
	NodeView nodeView;
	MainView mainView;
	ConnectView conView;
	CameraView camView;
	MapView mapView;
	P2PView p2pView;
	JPanel views;
	//JLabel status;
	StatusPanel status;
	
	final String NODEVIEW = "Node View";
	final String MAINVIEW = "Main View";
	final String MAPVIEW = "Map View";
	final String CONVIEW = "Connect View";
	final String CAMVIEW = "Camnera View";
	final String P2PVIEW = "P2P View";
	

	final String DATADIR = "dataDir";
	String spdFile = DATADIR + "/sdmay1207.spd";
	boolean networkRunning;

	

	public NetbookFrame() {

		networkRunning = false;
		
		//status = new JLabel("> Welcome");
		status = new StatusPanel();

		// Create the GUI Frame
		mainView = new MainView(this, nodeNum);
		nodeView = new NodeView(this);
		conView = new ConnectView(this);
		camView = new CameraView(this);
		mapView = new MapView(this);
		p2pView = new P2PView(this);

		views = new JPanel(new CardLayout());
		views.add(conView, CONVIEW);
		views.add(mainView, MAINVIEW);
		views.add(nodeView, NODEVIEW);
		views.add(camView, CAMVIEW);
		views.add(mapView, MAPVIEW);
		views.add(p2pView, P2PVIEW);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(views, BorderLayout.CENTER);
		panel.add(status, BorderLayout.SOUTH);

		this.getContentPane().add(panel);
		this.setSize(800, 525);

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				System.out.println("Exiting");
				stopNetwork();
				System.exit(0);
			}
		});

	}

	
	
	
	public void createNetwork() {
		nc = new NodeController(nodeNum, DATADIR);
		nc.p2pCmdr.setGUI(p2pView);
		nc.addNetworkObserver(this);
		nc.networkRejoinMonitor.addListener(this);
	
		nodes = getNodes();

		
		
		nc.addSensor(new BatterySensor());
		nc.addSensor(new NetbookGPS());
		startNetwork();
	}

	public void startNetwork() {
		//nc.start(RoutingAlg.BATMAN);
		nc.start(RoutingAlg.AODV);
		networkRunning = true;
		mainView.startStop.setText("Stop");
	}

	public void stopNetwork() {
		if (nc != null) {
			nc.stop();
			networkRunning = false;
			mainView.startStop.setText("Start");
		}
	}

	
	
	
	
	public void actionPerformed(ActionEvent action) {
		if (action.getSource() == conView.connect) {
			nodeNum = conView.getNodeNum();
			if (nodeNum >= 0) {
				createNetwork();
				nodeView.createNodes(nodes);
				for(Node n : nodes.values()) mapView.addNode(n);
				mainView.updateNodeNum(nodeNum);
				((CardLayout) views.getLayout()).show(views, MAINVIEW);
				System.out.println("Connect Clicked");
			}

		} else if (action.getSource() == mainView.startStop) {
			if (mainView.startStop.getText().equals("Start")) {
				startNetwork();
			} else {
				stopNetwork();
			}
			System.out.println("Start/Stop Clicked");

		} else if (action.getSource() == mainView.nodeView) { // Node View
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			System.out.println("Node View Clicked");

		} else if (action.getSource() == mainView.camView) { // Cam View
			((CardLayout) views.getLayout()).show(views, CAMVIEW);
			System.out.println("Cam View Clicked");

		} else if (action.getSource() == mainView.mapView) { // Map View
			((CardLayout) views.getLayout()).show(views, MAPVIEW);
			System.out.println("Map View Clicked");

		} else if (action.getSource() == mainView.p2pBtn) { // P2P
			((CardLayout) views.getLayout()).show(views, P2PVIEW);
			//mapView.setP2P(true);
			//p2pView.start();
			System.out.println("P2P Clicked");

		} else if (action.getSource() == mainView.nodeInfo) { // Node Info
			((CardLayout) views.getLayout()).show(views, NODEVIEW);
			nodeView.openNode(nodeNum);
			System.out.println("Node Info Clicked");
			
		} else if (action.getSource() == mainView.clearBtn) { // Clear Button
			mainView.clearPanel();
			System.out.println("Clear Button Clicked");

		} else if (action.getSource() == camView.playBtn) { // Play Button
			if (new File(spdFile).exists()) {
				System.out.println("File FOUND!");
				camView.play(spdFile);
			} else {
				System.out.println("File not found");
			}

		} else if (action.getSource() == nodeView.backBtn
				|| action.getSource() == mapView.backBtn
				|| action.getSource() == p2pView.backBtn
				|| action.getSource() == camView.backBtn) {
			((CardLayout) views.getLayout()).show(views, MAINVIEW);
			System.out.println("Back Button Clicked");
		}
	}

	public void update(Observable observable, Object obj) {
		NetworkEvent netEvent = (NetworkEvent) obj;
		Node node = null;
		switch (netEvent.event) {
		case RecvdHeartbeat:
		case SentHeartbeat:
			Heartbeat hb = (Heartbeat) netEvent.data;
			System.out.println("Got heartbeat from " + hb.from + ": "
					+ hb.toString());
			node = nodes.get(hb.from);
			if (node != null) {
				nodes.get(hb.from).newHeartbeat(hb);
				// mapView.updateNodes(nodes.get(hb.from));
			} else {
				System.out.println("Heartbeat received from " + netEvent.data
						+ "\n\tIt was not in system\n\tAdding it now");
				mainView.displayMessage("Heartbeat received from "
						+ netEvent.data
						+ "\n\tIt was not in system\n\tAdding it now");
				nodes.put(hb.from, new Node(hb.from));
				node = nodes.get(hb.from);
				node.newHeartbeat(hb);
				nodeView.addNode(node);
				mapView.addNode(node);
			}
			node.setConnection(true);
			break;

		case NodeJoined:
			System.out.println("Received a Node joined message from "
					+ netEvent.data);
			node = nodes.get((Integer) netEvent.data);
			if (node == null) {
				node = new Node((Integer) netEvent.data);
				nodes.put((Integer) netEvent.data, node);
				mainView.displayMessage("Node joined: " + netEvent.data);
				nodeView.addNode(node);
				// mapView.updateNodes(node);
				mapView.addNode(node);
				node.setConnection(true);
			} else {
				node.setConnection(true);
			}
			setStatus("Node joined: "+netEvent.data);
			break;

		case NodeLeft:
			System.out.println("Received a node left from " + netEvent.data);
			mainView.displayMessage("Node left: " + netEvent.data);
			nodes.get((Integer) netEvent.data).setConnection(false);
			setStatus("Node Left: "+netEvent.data);
			break;

		case RecvdTextMessage:
			System.out.println("Received Text message");
			TextMessage tm = (TextMessage) netEvent.data;
			mainView.displayMessage(tm.from, nodeNum, tm.message);
			//nodes.get(tm.from).addMessage(tm.from, tm.message);
			setStatus("Received Text Message");
			
		}
	}

	
	
	// NetworkRejoinListenerMethods
    public void lostSingleNode()
    {
        // ignore
    }

    public void networkSplit(final Location p){
    	System.out.println("Network Split received, moving to "+p+" to rejoin");
        String message = "We lost several nodes at once - you should move to "+p+" to rejoin them.";
        p2pView.setLocation(message, p);
        changeView(P2PVIEW, nodeNum);
    }

    public void lostEntireNetwork(final Location p){
    	System.out.println("Received \"lostEntireNetwork\" move to "+p+" to rejoin");
        String message = "The entire network has become disconnected - you should move to "+p+" to rejoin it.";
        p2pView.setLocation(message, p);
        changeView(P2PVIEW, nodeNum);
    }


	
	
	
	public void sendMessage(int nodeDest, String text) {
		TextMessage txtMsg = new TextMessage(text);
		nc.sendNetworkMessage(txtMsg, nodeDest);
	}

	public void sendMessageToAll(String text) {
		TextMessage txtMsg = new TextMessage(text);
		nc.broadcastNetworkMessage(txtMsg);
	}

	public Map<Integer, Node> getNodes() {
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		for (int key : nc.getNodesInNetwork().keySet()) {
			nodes.put(key, new Node(key));
		}
		return nodes;
	}

	public void changeView(String view, int nodeNumber) {
		((CardLayout) views.getLayout()).show(views, view);
		if (view == NODEVIEW) {
			nodeView.openNode(nodeNumber);
			System.out.println("Changed view to node view");
		} else if (view == MAPVIEW) {
			mapView.showNode(nodeNumber);
			System.out.println("Changed view to map view");
		}
	}
	
	public int getThisNodeNumber() {
		return nodeNum;
	}

	public Location getThisNodeLocation(){
		Node node = nodes.get(nodeNum);
		return new Location(node.getLatitude(), node.getLongitude());
	}
	
	public Point2PointCommander getP2PCommander(){
		if(nc!=null){
			return nc.p2pCmdr;
		} else {
			return null;
		}
	}

	public void setStatus(String message) {
		status.addStatus("> " + message);
	}

	
	
	
	
	public void run() {
	}

	public static void main(String[] args) {
		NetbookFrame gui = new NetbookFrame();
		// gui.start();
		gui.setVisible(true);
	}

}
