package netbook;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CameraView extends JPanel{

	private static final long serialVersionUID = 1L;
	
	NetbookFrame parent;
	
	JButton backBtn;
	JButton playBtn;
	
	//JLabel receiveFrom;
	JLabel errorLabel;
	//JTextField camNode;
	
	String defaultNodeText;
	Font regFont;
	
	public CameraView(NetbookFrame parent){
		this.parent = parent;
		
		defaultNodeText = "Dest #";
		regFont = new Font("Serif", Font.PLAIN, 12);
				
		//receiveFrom = new JLabel("Node to receive camera footage from:");
		//receiveFrom.setForeground(Color.DARK_GRAY);
		//receiveFrom.setFont(regFont);
		
		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.DARK_GRAY);
		errorLabel.setFont(regFont);
		
		//camNode = new JTextField(defaultNodeText);
		//camNode.setFont(regFont);
		//camNode.setColumns(5);
		//camNode.setBackground(Color.LIGHT_GRAY);		
		
		backBtn = new JButton("Back");
		backBtn.setBackground(Color.GRAY);
		backBtn.addActionListener(parent);

		playBtn = new JButton("Play Stream");
		playBtn.setBackground(Color.GRAY);
		playBtn.addActionListener(parent);
		
		//this.add(receiveFrom);
		//this.add(camNode);
		this.add(errorLabel);
		this.add(playBtn);
		this.add(backBtn);
	}
	
	public void play(String fileName){
		openCameraReceiving(fileName);
		/*
		 try{
		 
			int recNode = Integer.parseInt(camNode.getText());
			if(recNode > 0 && recNode < 255){
				//openCameraReceiving(writeNewSPDFile(recNode));
				openCameraReceiving(fileName);
			}
		} catch (Exception e){
			errorLabel.setText("Camera node not a number");
		}
		*/
	}
	
	/*
	private String writeNewSPDFile(int recNode){
		try{
			FileOutputStream fos = new FileOutputStream(fileName);
	
			fos.write("v=0\n".getBytes());
			fos.write(("o=- 1234 3 IN IP4 192.168.2."+recNode+"\n").getBytes());
			fos.write("s=Streamer\n".getBytes());
			fos.write("t=0 0\n".getBytes());
			fos.write("m=video 7476 RTP/AVP 74\n".getBytes());
			fos.write("a=rtpmap:74 H263-1998/90000\n".getBytes());
			fos.write("a=fmtp:74 profile=0; level=40\n".getBytes());
			return fileName;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	*/

	
	
	private void openCameraReceiving(String fileName){
		//Process process;
		try {
			//process = 
			System.out.println("Starting VLC player with file: ");
			Runtime.getRuntime().exec("vlc-wrapper "+fileName);
	        //InputStream in = process.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

}
