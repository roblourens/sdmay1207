package netbook.textmessenger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import netbook.NetbookFrame;

public class TextPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	JButton sendBtn;
	JButton sendToAllBtn;
	JLabel msgLabel;
	JLabel nodeLabel;
	JTextField msgField;
	JTextField nodeDestField;
	JTextArea textArea;
	
	Font regFont;

	
	int nodeNum;
	
	NetbookFrame parent;
	
	public TextPanel(NetbookFrame parent, int nodeNum){
		
		this.nodeNum = nodeNum;
		this.parent = parent;
		
		regFont = new Font("Serif", Font.PLAIN, 12);

		
		
		sendBtn = new JButton("Send");
		sendBtn.setFont(regFont);
		sendBtn.setBackground(new Color(45, 100, 54));
		sendBtn.addActionListener(this);
		
		sendToAllBtn = new JButton("Send To All");
		sendToAllBtn.setFont(regFont);
		sendToAllBtn.setBackground(new Color(45, 100, 54));
		sendToAllBtn.addActionListener(this);
		
		msgLabel = new JLabel("     Message:");
		msgLabel.setFont(regFont);
		
		msgField = new JTextField();
		msgField.setColumns(30);
		msgField.setFont(regFont);
		msgField.setBackground(Color.WHITE);
		
		nodeLabel = new JLabel("  Dest Num:");
		nodeLabel.setFont(regFont);
		
		nodeDestField = new JTextField();
		nodeDestField.setFont(regFont);
		nodeDestField.setColumns(5);
		nodeDestField.setBackground(Color.WHITE);		
		
		textArea = new JTextArea();
		textArea.setFont(regFont);
		textArea.setBackground(Color.GRAY);
		textArea.setColumns(10);
		textArea.setRows(15);
		
		
		JPanel label = new JPanel();
		JPanel text = new JPanel();
		JPanel buttons = new JPanel();
		
		label.setLayout(new GridLayout(0, 1));
		text.setLayout(new GridLayout(0, 1));
		buttons.setLayout(new GridLayout(0, 1));
		
		label.add(msgLabel);
		label.add(nodeLabel);
		text.add(msgField);
		text.add(nodeDestField);
		buttons.add(sendBtn);
		buttons.add(sendToAllBtn);
		
		JPanel control = new JPanel();
		control.setLayout(new BorderLayout());
		control.add(label, BorderLayout.WEST);
		control.add(text, BorderLayout.CENTER);
		control.add(buttons, BorderLayout.EAST);
		
		
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(textArea), BorderLayout.CENTER);
		this.add(control, BorderLayout.SOUTH);
	}

	public void addMessage(int nodeSrc, int nodeDest, String msg){
		textArea.append(nodeSrc+" -> "+nodeDest+": "+ msg+"\n");
	}

	public void addMessage(String msg){
		textArea.append("> "+msg+"\n");
	}
	
	public void setDestination(int nodeNum){
		nodeDestField.setText(""+nodeNum);
	}
	
	
	public void actionPerformed(ActionEvent action) {
		if(action.getSource() == sendBtn){
			int nodeDest = Integer.parseInt(nodeDestField.getText());
			addMessage(nodeNum, nodeDest, msgField.getText());
			parent.sendMessage(nodeDest, msgField.getText());
			msgField.setText("");
		
		} else if(action.getSource() == sendToAllBtn){
			addMessage(nodeNum, 255, msgField.getText());
			parent.sendMessageToAll(msgField.getText());
			msgField.setText("");
		}
	}
	
	public void updateNodeNumber(int nodeNumber){
		nodeNum = nodeNumber;
	}

	public void clear() {
		textArea.setText("");
	}
	
	/*
	public static void main(String... args){
		JFrame frame = new JFrame();
		frame.setContentPane(new TextPanel(null, 23));
		frame.setVisible(true);
		
	}
	*/
}
