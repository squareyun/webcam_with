package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainClient {
	Socket socket;
	public static JLabel label;
	
	public void startClient(String IP, int port) {
		label = new JLabel();
		JFrame frame = new JFrame();
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		label = new JLabel();
		label.setSize(1000, 1000);
		label.setVisible(true);
		
		frame.add(label);
		frame.setVisible(true);
		
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
					receiveVideo();
				} catch (Exception e) {
					stopClient();
					System.out.println("[서버 접속 실패]");
				}
			}
		};
		thread.start();
	}
	
	protected void receiveVideo() throws IOException {
		while(true) {
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				label.setIcon((ImageIcon)in.readObject());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	public static void main(String[] args) {
		MainClient c = new MainClient();
		c.startClient("192.168.219.101", 55555);
	}
}
