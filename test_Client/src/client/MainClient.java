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
import java.awt.*;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainClient {
	Socket socket;
	Socket msgSocket;
	public static JLabel label;
	public static JFrame frame;
	public static ObjectInputStream in;

	TextField txt1 = new TextField("채팅");
	TextField txt2 = new TextField("");
	TextField txt3 = new TextField("");
	TextArea lbl = new TextArea("채팅 내역");

	public void startClient(String IP, int port, int msgPort) {
		
		label = new JLabel();
		frame = new JFrame();
		frame.setTitle("Client");
		JButton btn4 = new JButton("전송");
		JButton btn1 = new JButton("나가기");
		JButton btn2 = new JButton("문제 변경");
		JButton btn3 = new JButton("??");
		frame.setLayout(null);
		lbl.setBounds(10,500,620,200); //채팅내역
		txt1.setBounds(10,710,520,40); //채팅치는곳
		txt2.setBounds(670,70,200,410); //점수판
		txt3.setBounds(720,20,100,40);
		btn4.setBounds(530,710,100,40);
		btn1.setBounds(720,700,100,40);
		btn2.setBounds(720,630,100,40);
		btn3.setBounds(720,560,100,40);
		//프레임에 컴포넌트 추가
		frame.add(lbl);
		frame.add(txt1);
		frame.add(txt2);
		frame.add(txt3);
		frame.add(btn4);
		frame.add(btn1);
		frame.add(btn2);
		frame.add(btn3);
		//프레임 보이기
		
		Dimension d = new Dimension(900, 800);
		frame.setPreferredSize(d);
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String message = txt1.getText();
				send(message);
			}
		});
		
		frame.pack();	
		label = new JLabel();
		label.setSize(640, 480);
		label.setVisible(true);
		
		frame.add(label);
		frame.setVisible(true);

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket(IP, port);
					msgSocket = new Socket(IP, msgPort);
					System.out.println("[서버 접속 성공]");
					receiveVideo();
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
			System.out.println("1번 에러");
			e1.printStackTrace();
			stopClient();
		}
		while(true) {
			try {
				try {
					label.setIcon((ImageIcon)in.readObject());
				} catch (IOException e) {
					System.out.println("2번 에러");
					e.printStackTrace();
				}

			} catch (ClassNotFoundException e) {
				System.out.println("4번 에러");
				e.printStackTrace();
			}
		}
	}

	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				frame.dispose();
				socket.close();
				msgSocket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						System.out.println("receive() on client 1");
						InputStream msgIn = msgSocket.getInputStream();
						System.out.println("receive() on client 2");
						byte[] buffer = new byte[512];
						System.out.println("receive() on client 3");
						int length = msgIn.read(buffer);
						System.out.println("receive() on client 4");
						if(length == -1) throw new IOException();
						System.out.println("receive() on client 5");
						String message = new String(buffer, 0, length, "UTF-8");
						System.out.println("receive() on client 6");
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
			@Override
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
		MainClient c = new MainClient();
		c.startClient("localhost", 55555, 44444);
		c.receive();
	}
}
