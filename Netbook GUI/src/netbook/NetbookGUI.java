package netbook;

import java.awt.BorderLayout;
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

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;

public class NetbookGUI extends JFrame implements ActionListener, Runnable, Observer{

	private static final long serialVersionUID = 1L;

	
	Thread runner;
	int nodeNum;
	Map<Integer, Node> nodes;
	NodeController nc;
	NodeView nodeView;
	
	public NetbookGUI(){
		
		// Establish networking
		Random r = new Random();
		nodeNum = r.nextInt(245) + 10;
		
		//final String dataDir = "dataDir";
		//nc = new NodeController(nodeNumber, dataDir);
		//nc.addNetworkObserver(this);
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
		
		
		
		// Create the GUI Frame
		this.setLayout(new BorderLayout());
			   
		JPanel panel = new MainPanel(this, nodeNum, nodes);
		this.add(panel, BorderLayout.CENTER);
		this.setSize(1000, 800);
		
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	System.out.println("Exiting");
		    	System.exit(0); 
		    }
		});
	}
	
	
	
	





	public void start() {
	    if ( runner == null ) {
	        runner = new Thread( this );
	        runner.start();
	    }
	}
	
	public void stop() {
		if ( runner != null && runner.isAlive()) runner.interrupt();
	    runner = null;
	}
	
	@Override
	public void run() {
		while(runner != null){
			
		}
		
	}

	
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource()== MainPanel.startStop){
			
		} else if(action.getSource() == MainPanel.mapView){
			
		} else if(action.getSource() == MainPanel.nodeView){
			nodeView = new NodeView(this, nodes);
			this.setContentPane(nodeView);
		} else if(action.getSource() == MainPanel.nodeInfo){
			nodeView = new NodeView(this, nodes);
			openNode(nodeNum);			
		}
	}

	public void openNode(int nodeNumber){
		//Heartbeat hb = nc.getNodesInNetwork().get(nodeNum).getHeartbeat();
		this.setContentPane(nodeView);
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
	            break;
	        case NodeJoined:
	            System.out.println("Node joined: " + netEvent.data);
	            break;
	        case NodeLeft:
	            System.out.println("Node left: " + netEvent.data);
	            break;
        }
    }
	
	
	
	
	
	public static void main(String[] args) {
		NetbookGUI gui = new NetbookGUI();
		gui.start();
		gui.setVisible(true);
	}


	
}
