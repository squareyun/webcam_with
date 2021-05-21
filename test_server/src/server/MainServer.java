package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.*;

public class MainServer {

    public static void main(String[] args) {
    	ServerSocket server;
		try {
			server = new ServerSocket(4000);
	    	System.out.println("waiting..");
	    	
	    	Socket socket = server.accept();
	    	System.out.println("connected..");
	    	
	    	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
	    	
	    	JLabel label = new JLabel();
	    	JFrame frame = new JFrame();
	    	frame.setSize(640, 360);
	    	frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
	    	
	    	label = new JLabel();
	    	label.setSize(640,360);
	    	label.setVisible(true);
	    	
	    	frame.add(label);
	    	frame.setVisible(true);
	    	
	    	while(true) {
	    		try {
					label.setIcon((ImageIcon)in.readObject());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }

}