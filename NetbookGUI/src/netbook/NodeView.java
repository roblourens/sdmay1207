package netbook;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JButton;
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
	
	JButton backBtn;
	
	
	public NodeView(NetbookGUI parent, Map<Integer, Node> nodes){
		this.parent = parent;
		
		JList nodeList = getNodeList(nodes);
		tabs = new JTabbedPane();
		tabs.setBackground(Color.GREEN);
		
		// Set up Split pane
		this.setDividerSize(5);
		this.setContinuousLayout(true);
		this.setRightComponent(tabs);
		this.setLeftComponent(createListPanel(nodeList));

	}

	private JPanel createListPanel(JList nodeList) {
		JPanel retPanel = new JPanel();
		retPanel.setLayout(new BorderLayout());
		retPanel.add(new JScrollPane(nodeList), BorderLayout.CENTER);
		
		backBtn = new JButton("Back");
		backBtn.addActionListener(parent);
		backBtn.setBackground(Color.GRAY);
		retPanel.add(backBtn, BorderLayout.SOUTH);
		
		return retPanel;		
	}

	public void openNode(Heartbeat heartbeat, int nodeNum){
		JPanel nodePanel = new NodePanel(heartbeat, nodeNum, this);
		tabs.add("Node "+nodeNum, nodePanel);
		tabs.setSelectedIndex(tabs.indexOfTab("Node "+nodeNum));
	}
	
	
	private JList getNodeList(Map<Integer, Node> nodes){
		int count = 0;
		String[] nodeStr = new String[nodes.size()];
		for(int i=0; i < 255 && count < nodes.size(); i++){
			if(nodes.containsKey(i)){
				nodeStr[count] = "Node "+i;
				count+= 1;
			}
		}
		
		final JList retList = new JList(nodeStr);
		retList.setBackground(Color.GRAY);
		retList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        retList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                   String val = (String) retList.getSelectedValue();
                   int nodeNum = Integer.parseInt(val.split(" ")[1]);
                   int index = tabs.indexOfTab("Node "+nodeNum);
                   if(index >= 0){
                	  tabs.setSelectedIndex(index); 
                   } else {
                	   parent.openNode(nodeNum);
                   }
                }
            }
        });
        
		return retList;
	}

	public void closeTab(int nodeNum) {
		tabs.remove(tabs.indexOfTab("Node "+nodeNum));
	}

	public void heartbeatUpdate(Heartbeat hb) {
		int nodeNum = hb.from;
		NodePanel node = (NodePanel) tabs.getComponentAt(tabs.indexOfTab("Node "+nodeNum));
		node.newHeartbeat(hb);
	}
}
