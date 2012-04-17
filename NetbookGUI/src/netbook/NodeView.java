package netbook;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import netbook.node.Node;
import netbook.node.NodePanel;
import netbook.textmessenger.InsetTextPanel;

public class NodeView extends JSplitPane {

	private static final long serialVersionUID = 1L;
		
	JTabbedPane tabs;
	JList nodeList;
	DefaultListModel listModel;
	NetbookFrame parent;
	InsetTextPanel textPanel;
	
	NodePanel[] nodePanels;
	
	JButton backBtn;
	
	
	public NodeView(final NetbookFrame parent){
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
                	int nodeNum = Integer.parseInt(val.split(" ")[2]);
                	openNode(nodeNum);
                }
            }
        });
	
		nodePanels = new NodePanel[255];
		
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
			listModel.addElement("Node ID "+key);
			nodePanels[key] = new NodePanel(nodeMap.get(key), this);
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
        int index = tabs.indexOfTab("Node ID "+nodeNum);
        if(index >= 0){
     	  tabs.setSelectedIndex(index); 
        } else if(nodePanels[nodeNum]!=null){
        	index = 0;
        	while(index < tabs.getTabCount()){
        		if(((NodePanel) tabs.getComponentAt(index)).getNodeNumber() > nodeNum){
        			break;
        		}
        		index++;
        	}
        	
        	tabs.add(nodePanels[nodeNum], index);
        	tabs.setTitleAt(index, "Node ID "+nodeNum);
        	tabs.setSelectedIndex(tabs.indexOfTab("Node ID "+nodeNum));
        }
	}
	
	

	public void closeNode(int nodeNum) {
		tabs.remove(tabs.indexOfTab("Node ID "+nodeNum));
	}

	public void addNode(Node node){
		int nodeNum = node.getNodeNumber();
		String id = "Node ID "+nodeNum;
		
		if(!listModel.contains(id)){
			listModel.addElement(id);
			
			int num = listModel.getSize();
			String elements[] = new String[num];
			int numbers[] = new int[num];
			for(int i=0; i<num; i++){
				elements[i] = (String) listModel.getElementAt(i);
				numbers[i] = Integer.parseInt(elements[i].split(" ")[2]);
			}
			Arrays.sort(numbers);
			
			for(int i=0; i<num; i++){
				listModel.setElementAt("Node ID "+numbers[i],i);
			}
			
		}
		
		if(nodePanels[nodeNum]==null){
			nodePanels[nodeNum] = new NodePanel(node, this);
		}		
		//nodePanels[nodeNum].setConnection(true);
	}
	
	public void removeNode(int nodeNum){
		//listModel.removeElement("Node "+data);
		//NodelistModel.getElementAt(listModel.indexOf("Node"+data));
		//if(nodePanels[nodeNum]!=null){
		//	nodePanels[nodeNum].setConnection(false);
		//}
	}


	
	public void sendMessage(int number, String message) {
		parent.sendMessage(number, message);
		this.closeTextMessenger();
	}

	public void sendMessageToAll(String message) {
		parent.sendMessageToAll(message);
		this.closeTextMessenger();
	}
	
	public void openMap(int nodeNum){
		parent.changeView(parent.MAPVIEW, nodeNum);
	}
	public void closeTextMessenger() {
		textPanel.setVisible(false);
		textPanel = null;
		
	}

}
