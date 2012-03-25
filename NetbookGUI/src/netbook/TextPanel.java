package netbook;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TextPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	JButton sendBtn;
	JButton sendToAllBtn;
	JTextField msgField;
	JTextField nodeDestField;
	JTextArea textArea;
	
	Font regFont;
	String defaultMsgText;
	String defaultNodeText;
	
	int nodeNum;
	
	NetbookGUI parent;
	
	public TextPanel(NetbookGUI parent, int nodeNum){
		
		this.nodeNum = nodeNum;
		this.parent = parent;
		
		regFont = new Font("Serif", Font.PLAIN, 12);
		defaultMsgText = "Enter Message Here";
		defaultNodeText = "Dest #";
		
		
		sendBtn = new JButton("Send");
		sendBtn.setFont(regFont);
		sendBtn.setBackground(new Color(45, 100, 54));
		sendBtn.addActionListener(this);
		
		sendToAllBtn = new JButton("Send To All");
		sendToAllBtn.setFont(regFont);
		sendToAllBtn.setBackground(new Color(45, 100, 54));
		sendToAllBtn.addActionListener(this);
		
		msgField = new JTextField(defaultMsgText);
		msgField.setColumns(30);
		msgField.setFont(regFont);
		msgField.setBackground(Color.LIGHT_GRAY);
		
		nodeDestField = new JTextField(defaultNodeText);
		nodeDestField.setFont(regFont);
		nodeDestField.setColumns(5);
		nodeDestField.setBackground(Color.LIGHT_GRAY);		
		
		textArea = new JTextArea();
		textArea.setFont(regFont);
		textArea.setBackground(Color.GRAY);
		textArea.setColumns(50);
		textArea.setRows(15);
		
		this.add(textArea);
		this.add(msgField);
		this.add(nodeDestField);
		this.add(sendBtn);
		this.add(sendToAllBtn);
	}

	public void addMessage(int nodeSrc, int nodeDest, String msg){
		textArea.append(nodeSrc+" -> "+nodeDest+": "+ msg+"\n");
	}

	public void addMessage(String msg){
		textArea.append("> "+msg+"\n");
	}
	
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == sendBtn){
			int nodeDest = Integer.parseInt(nodeDestField.getText());
			addMessage(nodeNum, nodeDest, msgField.getText());
			parent.sendMessage(nodeDest, msgField.getText());
			msgField.setText(defaultMsgText);
		
		} else if(action.getSource() == sendToAllBtn){
			addMessage(nodeNum, 255, msgField.getText());
			parent.sendMessageToAll(msgField.getText());
			msgField.setText(defaultMsgText);
		}
	}
	
}
