package client;

import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainClient {
	Socket socket;
	public static JLabel webcamLabel;
	public static JFrame frame;
	public static ObjectInputStream in;
	TextField chatField;
	TextField rankField;
	TextField txt3;
	TextArea chatLogArea;

	public void startClient(String IP, int port) {
		setGui();

		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
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
		while (true) {
			try {
				webcamLabel.setIcon((ImageIcon) in.readObject());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				stopClient();
			}
		}
	}

	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				frame.dispose();
				socket.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		while (true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				System.out.println("receive from server");
				chatLogArea.append(message);
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

	public void setGui() {
		frame = new JFrame();
		chatField = new TextField("채팅");
		rankField = new TextField("랭킹");
		txt3 = new TextField("");
		chatLogArea = new TextArea("채팅 내역");
		webcamLabel = new JLabel();
		frame.setTitle("Client");
		JButton exitBtn = new JButton("나가기");
		JButton btn2 = new JButton("문제 변경");
		JButton btn3 = new JButton("??");
		JButton sendBtn = new JButton("전송");

		rankField.setEditable(false);
		chatLogArea.setEditable(false);

		frame.setLayout(null);
		chatLogArea.setBounds(10, 500, 620, 200); // 채팅내역
		chatField.setBounds(10, 710, 520, 40); // 채팅치는곳
		rankField.setBounds(670, 70, 200, 410); // 점수판
		txt3.setBounds(720, 20, 100, 40);
		exitBtn.setBounds(720, 700, 100, 40);
		sendBtn.setBounds(530, 710, 100, 40);
		btn2.setBounds(720, 630, 100, 40);
		btn3.setBounds(720, 560, 100, 40);
		webcamLabel.setSize(640, 480);

		// 프레임에 컴포넌트 추가
		frame.add(webcamLabel);
		frame.add(chatField);
		frame.add(rankField);
		frame.add(txt3);
		frame.add(chatLogArea);
		frame.add(exitBtn);
		frame.add(btn2);
		frame.add(btn3);
		frame.add(sendBtn);

		// 프레임 보이기
		frame.setPreferredSize(new Dimension(900, 800));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		exitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		MainClient c = new MainClient();
		c.startClient("localhost", 55555);
	}
}
