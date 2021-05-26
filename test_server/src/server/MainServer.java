package server;

import java.awt.Image;
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
			
			//frame과 label 사이즈를 1000, 1000으로 해둠
			//client 코드에서 캠 크기 조절 테스트를 위해 임의로 바꿔둔 부분
			//필요에 따라 조절하여 사용
			JLabel label = new JLabel();
			JFrame frame = new JFrame();
			frame.setSize(1000, 700);
			frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
			
			label = new JLabel();
			label.setSize(640, 480);
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
