package netbook.map;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import netbook.MapView;

public class MapMouseListener implements MouseMotionListener, MouseListener{
	
	MapView parent;
	
	public MapMouseListener(MapView parent){
		this.parent = parent;
	}

	public void mouseDragged(MouseEvent e) { }

    public void mouseMoved(MouseEvent e) {
    	
    }

	@Override
	public void mouseClicked(MouseEvent click) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

}
