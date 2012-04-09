package netbook;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectView extends JPanel {

	private static final long serialVersionUID = 1L;
	
	JLabel nodeLabel;
	JLabel errorLabel;
	JTextField nodeNumStr;
	JButton connect;
	
	
	Color btnColor;
	Font btnFont;
	Font labelFont;
	Font textFont;
	
	public ConnectView(NetbookFrame parent){

		btnColor = new Color(45, 100, 54);
		btnFont = new Font("Serif", Font.PLAIN, 18);
		labelFont = new Font("Serif", Font.PLAIN, 36);
		textFont = new Font("Serif", Font.PLAIN, 36);
		
		nodeLabel = new JLabel("Node Number: ");
		nodeLabel.setFont(labelFont);
		nodeLabel.setForeground(btnColor);
		
		nodeNumStr = new JTextField();
		nodeNumStr.setColumns(30);
		nodeNumStr.setFont(textFont);
		nodeNumStr.setBackground(Color.LIGHT_GRAY);
		
		connect = new JButton("Connect");
		connect.setSize(20,20);
		connect.setFont(btnFont);
		connect.setBackground(btnColor);
		connect.addActionListener(parent);
		
		errorLabel = new JLabel("");
		errorLabel.setFont(textFont);
		errorLabel.setForeground(Color.red);
		
		this.setBackground(Color.GRAY);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(nodeLabel);
		this.add(nodeNumStr);
		this.add(Box.createRigidArea(new Dimension(0,200)));
		this.add(connect);
		this.add(Box.createRigidArea(new Dimension(0,100)));
		this.add(errorLabel);
	}

	public int getNodeNum() {
		try{
			int nodeNum = Integer.parseInt(nodeNumStr.getText());
			if(nodeNum >= 0 && nodeNum < 255){
				return nodeNum;
			} 
			errorLabel.setText("Number must be between 0 and 255!");
		} catch(Exception e){
			errorLabel.setText("Problem turning value into a number!");
		}
		return -1;
	}

}
