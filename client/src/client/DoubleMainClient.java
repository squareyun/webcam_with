package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class DoubleMainClient {
	Socket socket;
	Socket msgSocket;
	public static JLabel label;
	public static JFrame frame;
	public static ObjectInputStream in;
	
	public void startClient(String IP, int port, int msgPort) {
		label = new JLabel();
		frame = new JFrame();
		frame.setSize(700, 700);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		label = new JLabel();
		label.setSize(640, 480);
		label.setVisible(true);
		
		frame.add(label);
		frame.setVisible(true);
		
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					msgSocket = new Socket(IP, msgPort);
					System.out.println("[서버 접속 성공]");
					receiveVideo();
					receive();
				} catch (Exception e) {
					stopClient();
					System.out.println("[서버 접속 실패]");
				}
			}
		};
		thread.start();
	}
	
	public void receiveVideo() {
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			System.out.println("1번에러");
			e1.printStackTrace();
			stopClient();
		}
		while(true) {
			try {
				label.setIcon((ImageIcon)in.readObject());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				stopClient();
			}
		}
	}

	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				frame.dispose();
				socket.close();
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		Thread thread = new Thread() {
			public void run() {
				while(true) {
					try {
						InputStream msgIn = msgSocket.getInputStream();
						byte[] buffer = new byte[512];
						int length = msgIn.read(buffer);
						if(length == -1) throw new IOException();
						String message = new String(buffer, 0, length, "UTF-8");
						System.out.println(message);
					} catch (Exception e) {
						stopClient();
						break;
					}
				}
			}
		};
		thread.start();
	}
	
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = msgSocket.getOutputStream();
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
		DoubleMainClient c = new DoubleMainClient();
		c.startClient("localhost", 55555, 44444);
		Scanner scan = new Scanner(System.in);
		String msg;
		while((msg = scan.next()) != "Q") c.send(msg);
		scan.close();
	}
}
