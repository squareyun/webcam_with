package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainServer {

	public static void main(String[] args) {
		ServerSocket server;
		
		try {
			server = new ServerSocket(5000);
			System.out.println("wating...");
			
			Socket socket = server.accept();
			System.out.println("connected...");
			
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			JLabel label = new JLabel();
			JFrame frame = new JFrame();
			frame.setSize(700, 700);
			frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
			
			label = new JLabel();
			label.setSize(700, 700);
			label.setVisible(true);
			
			frame.add(label);
			frame.setVisible(true);
			
			while(true) {
				try {
					label.setIcon((ImageIcon)in.readObject());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
