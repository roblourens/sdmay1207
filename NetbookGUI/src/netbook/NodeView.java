package netbook;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.DefaultListModel;
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
	JList nodeList;
	DefaultListModel listModel;
	NetbookGUI parent;
	
	
	JButton backBtn;
	
	
	public NodeView(final NetbookGUI parent, Map<Integer, Node> nodes){
		this.parent = parent;
		
		listModel = new DefaultListModel();
		nodeList = new JList(listModel);
		nodeList.setBackground(Color.GRAY);
		nodeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        nodeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                	System.out.println("Node Clicked:"+nodeList.getSelectedValue());
                   String val = (String) nodeList.getSelectedValue();
                   int nodeNum = Integer.parseInt(val.split(" ")[1]);
                   parent.openNode(nodeNum);
                }
            }
        });
		
		getNodeList(nodes);
		
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
        int index = tabs.indexOfTab("Node "+nodeNum);
        if(index >= 0){
     	  tabs.setSelectedIndex(index); 
        } else {
        	JPanel nodePanel = new NodePanel(heartbeat, nodeNum, this);
        	tabs.add("Node "+nodeNum, nodePanel);
        	tabs.setSelectedIndex(tabs.indexOfTab("Node "+nodeNum));
        }
	}
	
	
	private void getNodeList(Map<Integer, Node> nodes){
		//int count = 0;
		//String[] nodeStr = new String[nodes.size()];
		//for(int i=0; i < 255 && count < nodes.size(); i++){
		//	if(nodes.containsKey(i)){
		//		nodeStr[count] = "Node "+i;
		//		count+= 1;
		//	}
		//}
		for(int key : nodes.keySet()){
			listModel.addElement("Node "+key);
		}
	}

	public void closeTab(int nodeNum) {
		tabs.remove(tabs.indexOfTab("Node "+nodeNum));
	}

	public void heartbeatUpdate(Heartbeat hb) {
		int nodeNum = hb.from;
		int index = tabs.indexOfTab("Node "+nodeNum);
		
		if(index >= 0){
			NodePanel node = (NodePanel) tabs.getComponentAt(index);
			node.newHeartbeat(hb);
		}
		
	}
	
	public void addNode(int data){
		listModel.addElement("Node "+data);
	}
	
	public void removeNode(int data){
		listModel.removeElement("Node "+data);
	}
}
