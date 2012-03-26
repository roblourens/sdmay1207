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
	
	NodePanel[] nodes;
	
	JButton backBtn;
	
	
	public NodeView(final NetbookGUI parent){
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
                	openNode(nodeNum);
                }
            }
        });
	
		nodes = new NodePanel[255];
		
		tabs = new JTabbedPane();
		tabs.setBackground(Color.GREEN);
		
		// Set up Split pane
		this.setDividerSize(5);
		this.setContinuousLayout(true);
		this.setRightComponent(tabs);
		this.setLeftComponent(createListPanel(nodeList));

	}
	
	public void createNodes(Map<Integer, Node> nodeMap){	
		for(int key : nodeMap.keySet()){
			listModel.addElement("Node "+key);
			nodes[key] = new NodePanel(key, this);
			nodes[key].setConnection(true);
		}
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

	public void openNode(int nodeNum){
        int index = tabs.indexOfTab("Node "+nodeNum);
        if(index >= 0){
     	  tabs.setSelectedIndex(index); 
        } else if(nodes[nodeNum]!=null){
        	tabs.add("Node "+nodeNum, nodes[nodeNum]);
        	tabs.setSelectedIndex(tabs.indexOfTab("Node "+nodeNum));
        }
	}
	
	

	public void closeNode(int nodeNum) {
		tabs.remove(tabs.indexOfTab("Node "+nodeNum));
	}

	public void heartbeatUpdate(Heartbeat hb) {
		int nodeNum = hb.from;
				
		if(nodes[nodeNum]!=null){
			nodes[nodeNum].newHeartbeat(hb);
			nodes[nodeNum].setConnection(true);
		} else {
			nodes[nodeNum] = new NodePanel(nodeNum,this);
			nodes[nodeNum].setConnection(true);
			nodes[nodeNum].newHeartbeat(hb);
		}
	}
	
	public void addNode(int nodeNum){
		String id = "Node "+nodeNum;
		
		if(!listModel.contains(id)){
			listModel.addElement(id);
		}
		
		if(nodes[nodeNum]==null){
			nodes[nodeNum] = new NodePanel(nodeNum, this);
		}
		
		nodes[nodeNum].setConnection(true);
	}
	
	public void removeNode(int nodeNum){
		//listModel.removeElement("Node "+data);
		//NodelistModel.getElementAt(listModel.indexOf("Node"+data));
		if(nodes[nodeNum]!=null){
			nodes[nodeNum].setConnection(false);
		}
	}
}
