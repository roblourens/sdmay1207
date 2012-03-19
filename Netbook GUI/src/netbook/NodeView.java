package netbook;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import sdmay1207.ais.network.model.Heartbeat;
import sdmay1207.ais.network.model.Node;

public class NodeView extends JSplitPane{

	private static final long serialVersionUID = 1L;
		
	JTabbedPane tabs;
	NetbookGUI parent;
	
	
	public NodeView(NetbookGUI parent, Map<Integer, Node> nodes){
		this.parent = parent;
		
		JList<String> nodeList = getNodeList(nodes);
		tabs = new JTabbedPane();
		tabs.setBackground(Color.GREEN);
		
		// Set up Split pane
		this.setDividerSize(5);
		this.setContinuousLayout(true);
		this.setRightComponent(tabs);
		this.setLeftComponent(new JScrollPane(nodeList));

	}

	public void openNode(Heartbeat heartbeat, int nodeNum){
		JPanel nodePanel = new NodePanel(heartbeat, nodeNum);
		tabs.add("Node "+nodeNum, nodePanel);
	}
	
	
	private JList<String> getNodeList(Map<Integer, Node> nodes){
		int count = 0;
		String[] nodeStr = new String[nodes.size()];
		for(int i=0; i < 255 && count < nodes.size(); i++){
			if(nodes.containsKey(i)){
				nodeStr[count] = "Node "+i;
				count+= 1;
			}
		}
		
		final JList<String> retList = new JList<String>(nodeStr);
		retList.setBackground(Color.BLUE);
		retList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        retList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                   String val = retList.getSelectedValue();
                   parent.openNode(Integer.parseInt(val.split(" ")[1]));
                }
            }
        });
        
		return retList;
	}
}
