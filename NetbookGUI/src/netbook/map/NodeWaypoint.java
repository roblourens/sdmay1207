package netbook.map;

import netbook.MapView;
import netbook.node.HoverPanel;
import netbook.node.Node;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;



public class NodeWaypoint extends Waypoint {

	MapView parent;
	JXMapViewer  map;
	Node node;
	public HoverPanel panel;
	
	
	public NodeWaypoint(MapView parent, JXMapViewer map, Node node) {
		this.parent = parent;
		this.node = node;
		panel = new HoverPanel(node, parent);
		map.add(panel);
		panel.setVisible(false);
	}
	
	public GeoPosition getLocation(){
		return new GeoPosition((double) node.getLatitude(), (double) node.getLongitude());
	}
	
	public Node getNode(){
		return node;
	}
	
	public HoverPanel getPanel(){
		return panel;
	}
		
	
}
