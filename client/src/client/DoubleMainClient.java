package client;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class DoubleMainClient {
	Socket socket;
	Socket msgSocket;
	public static JLabel webcamLabel;
	public static JFrame frame;
	public static ObjectInputStream in;
	
	JTextField chatField;
	JTextField txt3;
	JTextArea rankArea;
	JTextArea chatLogArea;
	
	public void startClient(String IP, int port, int msgPort) {
		setGui();
		
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
				webcamLabel.setIcon((ImageIcon)in.readObject());
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
	
	public void setGui() {
		frame = new JFrame();
		chatField = new JTextField("");
		rankArea = new JTextArea("");
		txt3 = new JTextField("");
		chatLogArea = new JTextArea("");
		webcamLabel = new JLabel();
		frame.setTitle("Client");
		JButton exitBtn = new JButton("나가기");
		JButton changeBtn = new JButton("문제 변경");
		JButton sendBtn = new JButton("전송");

		chatLogArea.setEditable(false); 	// 수정 불가능하게
		rankArea.setEditable(false);
		txt3.setEditable(false);
		chatLogArea.setLineWrap(true);	// 자동 줄바꿈
		rankArea.setLineWrap(true);
		
		
		frame.setLayout(null);
		chatLogArea.setBounds(10, 500, 620, 200); // 채팅내역
		chatField.setBounds(10, 710, 520, 30); // 채팅치는곳
		rankArea.setBounds(670, 70, 180, 410); // 점수판
		txt3.setBounds(720, 20, 100, 40);
		exitBtn.setBounds(680, 710, 140, 30);
		sendBtn.setBounds(530, 710, 100, 30);
		changeBtn.setBounds(680, 630, 140, 40);
		webcamLabel.setSize(640, 480);

		// 프레임에 컴포넌트 추가
		frame.add(webcamLabel);
		frame.add(chatField);
		frame.add(rankArea);
		frame.add(txt3);
		frame.add(chatLogArea);
		frame.add(exitBtn);
		frame.add(changeBtn);
		frame.add(sendBtn);

		// 프레임 보이기
		frame.setPreferredSize(new Dimension(880, 790));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		exitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		// 창 열렸을 때 chatFiled에 포커스 주기
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				chatField.requestFocus();
			}
		});
		

		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		DoubleMainClient c = new DoubleMainClient();
		c.startClient("localhost", 55555, 44444);
	}
}
