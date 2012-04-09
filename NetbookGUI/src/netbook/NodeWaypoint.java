package netbook;

import netbook.node.HoverPanel;
import netbook.node.Node;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;



public class NodeWaypoint extends Waypoint {

	MapView parent;
	Node node;
	HoverPanel panel;
	
	
	public NodeWaypoint(MapView parent, Node node) {
		this.parent = parent;
		this.node = node;
		panel = new HoverPanel(node, parent);
	}
	
	public GeoPosition getLocation(){
		return new GeoPosition((double) node.getLatitude(), (double) node.getLongitude());
	}
	
	public Node getNode(){
		return node;
	}
		
	
}
