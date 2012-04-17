package netbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import netbook.map.TileInfo;
import netbook.textmessenger.InsetTextPanel;
import netbook.textmessenger.TextMessengerListener;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;

import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.cc.Point2PointCommander;
import sdmay1207.cc.Point2PointCommander.GoToLocCommand;
import sdmay1207.cc.Point2PointCommander.P2PState;
import sdmay1207.cc.Point2PointCommander.Point2PointGUI;
import sdmay1207.cc.Point2PointCommander.TooFewNodesException;

public class P2PView extends JPanel implements ActionListener, MouseListener, Point2PointGUI, TextMessengerListener{
	
	private static final long serialVersionUID = 1L;
	
	JXMapKit kit;
	NetbookFrame parent;
	InsetTextPanel textPanel;
	Point2PointCommander p2pCmdr;
	GoToLocCommand command;
	Waypoint[] waypoints;
	GeoPosition home;
	String status;
	
	boolean p2pModeOn;
	int settingPoint;
	private Location[] selectedLocations;
	

	String fromIcon = "dataDir/icons/meIcon.png"; 
	String toIcon = "dataDir/icons/connectedIcon.png"; 
	String rallyIcon = "dataDir/icons/disconnectedIcon.png";
	String destIcon = "dataDir/icons/flag.png";

	JButton backBtn;
	private JButton undoBtn;
	private JButton startBtn;
	private JButton msgBtn;
	private JButton clearBtn;
			

	public P2PView(NetbookFrame parent){
		this.parent = parent;
		this.setLayout(new BorderLayout());
			
		selectedLocations = new Location[3];
		settingPoint = 0;
		waypoints = new Waypoint[4];
		status = "";
		
		
		home = new GeoPosition(42.029850, -93.651237);

	    

	    		
	    backBtn = new JButton("Back to Main View");
		backBtn.addActionListener(parent);
		backBtn.setBackground(new Color(16, 150, 70));

		
	    undoBtn = new JButton("Undo");
		undoBtn.addActionListener(this);
		undoBtn.setBackground(new Color(16, 150, 70));

		msgBtn = new JButton("Send Message");
		msgBtn.addActionListener(this);
		msgBtn.setBackground(new Color(16, 150, 70));

	    startBtn = new JButton("Start");
	    startBtn.addActionListener(this);
	    startBtn.setBackground(new Color(16, 150, 70));

	    clearBtn = new JButton("Clear");
	    clearBtn.addActionListener(this);
	    clearBtn.setBackground(new Color(16, 150, 70));


		JPanel ctrlbuttons = new JPanel();
	    ctrlbuttons.add(startBtn);
	    ctrlbuttons.add(clearBtn);
	    ctrlbuttons.add(undoBtn);
		
	    JPanel viewbuttons = new JPanel();
		viewbuttons.add(backBtn);
		viewbuttons.add(msgBtn);
		
	    JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(0,1));
		buttons.add(ctrlbuttons);
		buttons.add(viewbuttons);

		
		this.add(buttons, BorderLayout.SOUTH);
		
		setUpMap();
	    this.add(kit, BorderLayout.CENTER);
	}
	
	@SuppressWarnings("rawtypes")
	private void setUpMap(){
		
		kit = new JXMapKit();
		
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
		        
				for(int i=0; i<waypoints.length; i++){
					if(waypoints[i]!=null){
						ImageIcon icon = null;
						
			        	switch(i){
			        	case 0 :	icon = new ImageIcon(fromIcon); break;
			        	case 1 :	icon = new ImageIcon(toIcon); break;
			        	case 2 :	icon = new ImageIcon(rallyIcon); break;
			        	case 3 :	icon = new ImageIcon(destIcon); break;
			        	}
						
						Waypoint wp = waypoints[i];
				        Point2D gp_pt = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
				        Point gpPoint = new Point((int)gp_pt.getX()-rect.x, (int)gp_pt.getY()-rect.y);
			        	if(i!=3) icon.paintIcon(map, g, gpPoint.x-icon.getIconWidth()/2, gpPoint.y-icon.getIconHeight());
			        	else	icon.paintIcon(map, g, gpPoint.x-icon.getIconWidth(), gpPoint.y-icon.getIconHeight());
						
			        	//System.out.println("Painting Icon("+wp.getPosition().toString()+") at "+gpPoint.x+" "+gpPoint.y);
					}
				}
		        
		        g.setColor(Color.RED);
		        g.setFont(new Font("Serif", Font.BOLD, 16));
		        String[] lines = status.split("\n");
		        int row = 60;
		        for(String s : lines){ 
		        	g.drawString(s, 30, row);
		        	row+=30;
		        }
			}
		};
		kit.getMainMap().setOverlayPainter(painter);
		
		kit.getMainMap().addMouseListener(this);
	}

	
	public void startP2P(){
		if((this.p2pCmdr = parent.getP2PCommander()) != null){
			p2pModeOn = true;
			setupForPoint(settingPoint);
			for(Waypoint wp : waypoints) if(wp!=null) wp.setPosition(new GeoPosition(0, 0));
			settingPoint = 0;
		} else {
			setStatus("Network not started!Please start network");
		}
		kit.getMainMap().repaint();
	}
	public void stopP2P(){
		p2pModeOn = false;
		waypoints = new Waypoint[4];
		//for(Waypoint wp : waypoints) wp = null;	
	}
	
	public boolean inP2Pmode() {
		return p2pModeOn;
	}
	
	
	
	
	public void setP2PLocation(Point point) {
		System.out.println("Setting P2P point for " + settingPoint);
		Location loc = convertPointToLocation(point);
		selectedLocations[settingPoint] = loc;
		waypoints[settingPoint] = new Waypoint(new GeoPosition((double)loc.latitude, (double) loc.longitude));
		settingNextPoint();
	}
	private void settingNextPoint() {
		if (settingPoint == 2) {
			settingPoint = 0;
			p2pModeOn = false;
			
			try {
				p2pCmdr.initiateP2PTask(selectedLocations[0],
						selectedLocations[1], selectedLocations[2], 1000000);
				setStatus("p2p initiated, finishing");
				parent.setStatus("p2p initiated, finishing");
			} catch (TooFewNodesException tfne) {
				setStatus("Sorry, there are not enough nodes in the network to connect those two points.");
				System.out.println("p2p too few nodes");
				startBtn.setText("Start");
				return;
			}

			setStatus("Initializing point-to-point task!\nSending commands to all nodes in the network.");
			System.out.println(Arrays.toString(selectedLocations));
		
		} else {
			settingPoint++;
			setupForPoint(settingPoint);
		}
	}
	private void setupForPoint(int p) {
		switch (p) {
		case 0:
			setStatus("Tap the point to retrieve video from");
			break;
		case 1:
			setStatus("Tap the point to send video to");
			break;
		case 2:
			setStatus("Tap the rally point");
			break;
		default:
			break;
		}
		kit.getMainMap().repaint();
	}

	
	private void undoP2P(){
		if(settingPoint > 0){
			waypoints[settingPoint] = null;
			selectedLocations[settingPoint] = null;
			settingPoint--;
			setupForPoint(settingPoint);
		}
	}
	private void clearMap(){
		waypoints = new Waypoint[4];
		setStatus("");
		kit.getMainMap().repaint();
	}
	
	// Point2Point GUI Methods
	@Override
	public void p2pInitiated(GoToLocCommand command) {
		p2pModeOn = true;
		this.command = command;
		String message = ("Node "
                         + command.from
                         + " has initiated a point-to-point task.\nGo to "
                         + command.loc
                         + " to relay video.\n"
                         + (command.headNodeNum == parent.getThisNodeNumber() ? " You are the head node."
                                 : "")
                         + (command.tailNodeNum == parent.getThisNodeNumber() ? " You are the tail node."
                                 : ""));
		setLocation(message, command.loc);
		parent.changeView(parent.P2PVIEW, parent.getThisNodeNumber());
	}
	@Override
	public void stateChanged(P2PState newState){
        System.out.println("Entered " + newState.name() + " state");
        String response = "";
        switch (newState)
        {
        // enRoute is pretty much covered above
        case searching:
            response = "You have reached your assigned area, now try to join up with neighbors";
            break;
        case waiting:
        	response = "You've joined up with neighbors, now wait for the head and tail nodes to join";
            break;
        case ready:
        	response = "Tail node is ready - start streaming whenever you want";
            break;
        case enRouteToRallyPoint:
        	response = "Connecting to head and tail timed out - return to rally point";
        	setDirections(command.rallyPoint);
            break;
        case active:
        	response = "Now streaming";
            break;
        case inactive:
        	response = "The point-to-point task has finished";
        	stopP2P();
            break;
        }
        setStatus(response);
        parent.setStatus("Entered " + newState.name() + " state");
    }

	
	public void setLocation(String msg, Location loc){
		setDirections(command.loc);
		setStatus(msg);
		parent.setStatus(msg);
	}
	
	
	public void setStatus(String message){
		status = message;
		kit.getMainMap().repaint();
	}
	public void setDirections(Location loc){
		clearMap();
		waypoints[3] = new Waypoint(new GeoPosition((double)loc.latitude, (double) loc.longitude));
	}
	public void setDirectionsFull(Location loc){
		Location currentLocation = parent.getThisNodeLocation();
		List<Location> list = p2pCmdr.getWrangler().getPathBetweenPoints(currentLocation, loc);
		Graphics g = kit.getMainMap().getGraphics();
		Point currentPoint = convertLocationToPoint(currentLocation);
		for(Location location : list){
			Point nextPoint = convertLocationToPoint(location);
			g.drawLine(currentPoint.x,currentPoint.y,nextPoint.x, nextPoint.y);
			currentPoint = nextPoint;
		}
	}


	public Point convertPositionToPoint(GeoPosition gp){
		JXMapViewer map = kit.getMainMap();
        Rectangle rect = map.getViewportBounds();
		Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
		System.out.println("GTP: Converted "+gp.toString()+"to "+gp_pt.toString()+" rectx:"+rect.x+" recty:"+rect.y);
		return new Point((int)(gp_pt.getX()-rect.x), (int)(gp_pt.getY()-rect.y));
	}
	public Point convertLocationToPoint(Location loc){
		JXMapViewer map = kit.getMainMap();
        Rectangle rect = map.getViewportBounds();
		GeoPosition gp = new GeoPosition((double) loc.latitude, (double) loc.longitude);
		Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
		System.out.println("LTP: Converted "+gp.toString()+"to "+gp_pt.toString()+"rectx:"+rect.x+" recty:"+rect.y);
		return new Point((int)(gp_pt.getX()-rect.x), (int)(gp_pt.getY()-rect.y));
	}
	public Location convertPointToLocation(Point point){
		JXMapViewer map = kit.getMainMap();
		Rectangle rect = map.getViewportBounds();
		Point2D correctedPoint = new Point(point.x+rect.x, point.y+rect.y);
		GeoPosition gp = map.getTileFactory().pixelToGeo(correctedPoint, map.getZoom());
		System.out.println("PTL:Converted "+point.toString()+"to "+gp.toString()+"rectx:"+rect.x+" recty:"+rect.y+" MapH:"+map.getHeight()+" Map W:"+map.getWidth());
		return new Location(gp.getLatitude(), gp.getLongitude());
	}
	
	
	// Action listener Methods
	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource() == undoBtn) {
			if(p2pModeOn) undoP2P();
			
		} else if (action.getSource() == startBtn) {
			clearMap();
			if(p2pModeOn){
				stopP2P();
				startBtn.setText("Start");
			} else {
				startP2P();
				startBtn.setText("Stop");
			}
			
		} else if (action.getSource() == msgBtn){
			openTextMessenger(0);
			
		} else if (action.getSource() == clearBtn){
			if(!p2pModeOn) clearMap();
		}
		
	}

	
	
	// MouseListener Methods
	@Override
	public void mouseClicked(MouseEvent click) {
		System.out.println("Mouse click p2p at "+click.getPoint().x+", "+click.getPoint().y);
		if(p2pModeOn){
			setP2PLocation(click.getPoint());
		}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	
	// TextMessenger Listener Methods
	public void sendMessage(int number, String message) {
		parent.sendMessage(number, message);
		this.closeTextMessenger();
	}
	public void sendMessageToAll(String message) {
		parent.sendMessageToAll(message);
		this.closeTextMessenger();
	}
	public void closeTextMessenger() {
		textPanel.setVisible(false);
		textPanel = null;
	}
	public void openTextMessenger(int nodeNum) {
		textPanel = new InsetTextPanel(this, parent.getThisNodeNumber());
		this.add(textPanel, BorderLayout.EAST);
		textPanel.setDestination(nodeNum);	
		this.updateUI();
	}
	
	
	
}
