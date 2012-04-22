package netbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import netbook.map.MapMouseListener;
import netbook.map.NodeWaypoint;
import netbook.map.TileInfo;
import netbook.node.HoverPanel;
import netbook.node.Node;
import netbook.textmessenger.InsetTextPanel;
import netbook.textmessenger.TextMessengerListener;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

//@SuppressWarnings("unchecked")
public class MapView extends JPanel implements TextMessengerListener, MouseMotionListener{

	private static final long serialVersionUID = 1L;
	final NetbookFrame parent;
	JButton backBtn;
	JPanel hoverLabel;
	HoverPanel currentNode;
	MapMouseListener mouseListener;
	InsetTextPanel textPanel;
	
	@SuppressWarnings("rawtypes")
	WaypointPainter painter;
	JXMapKit kit;
	
	Map<Integer, NodeWaypoint> waypointMapper;
	//Map<Integer, HoverPanel> hoverMapper;
	GeoPosition home;
	
	String meIcon = "dataDir/icons/my_person.png"; 
	String connectedIcon = "dataDir/icons/other_person.png"; 
	String disconnectedIcon = "dataDir/icons/disappear_person.png"; 
			

	
	

	public MapView(NetbookFrame parent){
		this.parent = parent;
		this.setLayout(new BorderLayout());
		
		//hoverMapper = new HashMap<Integer, HoverPanel>();
		waypointMapper = new HashMap<Integer, NodeWaypoint>();

		home = new GeoPosition(42.029850, -93.651237);
		setUpMap();
	    this.add(kit, BorderLayout.CENTER);
	    
	    backBtn = new JButton("Back");
		backBtn.addActionListener(parent);
		backBtn.setBackground(new Color(16, 150, 70));
		this.add(backBtn, BorderLayout.SOUTH);
		
		kit.getMainMap().addMouseMotionListener(this);
		Timer timer = new Timer();
		timer.schedule(new Updater(), 2000);
	  }
	
	@SuppressWarnings("rawtypes")
	private void setUpMap(){
		
		kit = new JXMapKit();
		
		kit.getMainMap().setOverlayPainter(painter);
		kit.setMiniMapVisible(false);
		
		kit.setTileFactory(new DefaultTileFactory(new TileInfo()));
		
		kit.setZoom(3);
		
		kit.setAddressLocation(home); 
		kit.setAddressLocationShown(true);
		kit.setCenterPosition(home);
		
		
		WaypointPainter painter = new WaypointPainter() {
			@Override
			protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
		        Rectangle rect = map.getViewportBounds();
		        
				for (NodeWaypoint wp : waypointMapper.values()) {
					Node node = wp.getNode();
					ImageIcon icon = null;
					if(node.getNodeNumber() == parent.getThisNodeNumber()){
						//g.setColor(Color.GREEN);
						icon = new ImageIcon(meIcon);
					} else if(node.isConnected()){
						//g.setColor(Color.BLUE);
						icon = new ImageIcon(connectedIcon);
					} else {
						//g.setColor(Color.GREEN);
						icon = new ImageIcon(disconnectedIcon);
					}
					
			        Point2D gp_pt = map.getTileFactory().geoToPixel(wp.getLocation(), map.getZoom());
			        Point gpPoint = new Point((int)gp_pt.getX()-rect.x, (int)gp_pt.getY()-rect.y);
					icon.paintIcon(map, g, gpPoint.x-icon.getIconWidth()/2, gpPoint.y-icon.getIconHeight());		
					//System.out.println("Painting Icon["+node.getNodeNumber()+" ("+wp.getLocation().toString()+") at "+gpPoint.x+" "+gpPoint.y);
				}
			}
		};
		kit.getMainMap().setOverlayPainter(painter);

	}
	
	
	
	public boolean addNode(Node node){
		int number = node.getNodeNumber();
		if(waypointMapper.get(number)==null){
			waypointMapper.put(number, new NodeWaypoint(this, kit.getMainMap(), node));
			//hoverMapper.put(number, new HoverPanel(node, this));
			System.out.println("Adding node to map");
			return true;
		} else {
			return false;
		}
	}
		
	public void showNode(int nodeNum){
		Waypoint node = waypointMapper.get(nodeNum);
		kit.setCenterPosition(node.getPosition());
	}
	
	
	
	public void openNodeViewer(int nodeNum){
		parent.changeView(parent.NODEVIEW, nodeNum);
	}
	


	public void checkPopup(Point point){
		JXMapViewer map = kit.getMainMap();
        Rectangle rect = map.getViewportBounds();
        //System.out.print("Point: "+point.toString()+"\t");
        
        for(int num : waypointMapper.keySet()){
        	//System.out.print("Checking HoverPanel "+num);
        	NodeWaypoint waypoint = waypointMapper.get(num);
        	GeoPosition gp = waypoint.getPosition();
	        Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
	        Point gpPoint = new Point((int)gp_pt.getX()-rect.x, (int)gp_pt.getY()-rect.y);
	        
	        //HoverPanel hoverPanel = hoverMapper.get(num);
	        HoverPanel hoverPanel = waypoint.getPanel();
	        //HoverPanel hoverPanel = waypoint.panel;
	        
	        if(hoverPanel!=null){    
        		//int x = hoverPanel.getX();
        		//int y = hoverPanel.getY();
	        	System.out.print("X: "+hoverPanel.getX()+" Y:"+hoverPanel.getY()+" W:"+hoverPanel.getWidth()+" H:"+hoverPanel.getHeight()+"\t");
	        	if(hoverPanel.isVisible() 
	        			&& point.getX() > gpPoint.getX() 
	        			&& point.getX() < gpPoint.getX()+hoverPanel.getWidth() 
	        			&& point.getY() > gpPoint.getY()
	        			&& point.getY() < gpPoint.getY()+hoverPanel.getHeight()){
	        		
	      	
	        	} else if(gpPoint.distance(point) < 10) {
	        		//map.add(hoverPanel);
	        	    hoverPanel.setLocation(gpPoint);
	        		//hoverPanel.setLocation(200,200);
		            System.out.println(" Setting Visible");
		            hoverPanel.setVisible(true);
		            currentNode = hoverPanel;
		            break;
		        } else {
		        	//map.remove(hoverPanel);
		            hoverPanel.setVisible(false);
		            currentNode = null;
		            System.out.println(" Setting Invisible");
		        }
	        } 
        }
	}
	
	
	
	// TextMessengerListener Methods
	@Override
	public void sendMessage(int number, String message) {
		parent.sendMessage(number, message);	
		this.closeTextMessenger();
	}
	@Override
	public void sendMessageToAll(String message) {
		parent.sendMessageToAll(message);
		this.closeTextMessenger();
	}
	@Override
	public void closeTextMessenger() {
		textPanel.setVisible(false);
		textPanel = null;
	}
	@Override
	public void openTextMessenger(int nodeNum) {
		textPanel = new InsetTextPanel(this, parent.getThisNodeNumber());
		this.add(textPanel, BorderLayout.EAST);
		textPanel.setDestination(nodeNum);		
	}

	
	
	// MouseMotionListener Methods
	@Override
	public void mouseDragged(MouseEvent e) {}
	
@Override
	public void mouseMoved(MouseEvent e) {
		checkPopup(e.getPoint());
	}


	

	private class Updater extends TimerTask{
		@Override
		public void run() {
			System.out.println("Updating map");
			kit.getMainMap().repaint();
		}
	}

	
}
