package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class MainClient {
	static Socket socket;
	static Socket msgSocket;
	public static ObjectInputStream in;
	public static JFrame loginFrame;
	public static JFrame frame;

	public static JTextField idField;
	public static JButton submmitBtn;
	public static String userName;
	public static JLabel webcamLabel;
	JTextField chatField;
	JTextField category;
	JTextArea rankArea;
	static JTextArea chatLogArea;

	public void startClient(String IP, int port, int msgPort) {
		setGui();

		Thread thread = new Thread() {
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

	public static void stopClient() {
		try {
			frame.dispose();
			loginFrame.dispose();
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
			if (msgSocket != null && !msgSocket.isClosed()) {
				msgSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void receive() {
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						InputStream msgIn = msgSocket.getInputStream();
						byte[] buffer = new byte[512];
						int length = msgIn.read(buffer);
						if (length == -1)
							throw new IOException();
						String message = new String(buffer, 0, length, "UTF-8");
						chatLogArea.append(message + "\n");
						chatLogArea.setCaretPosition(chatLogArea.getDocument().getLength());
					} catch (Exception e) {
						stopClient();
						break;
					}
				}
			}
		};
		thread.start();
	}

	public static void send(String message) {
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
		category = new JTextField("");
		chatLogArea = new JTextArea(11, 1);
		webcamLabel = new JLabel();
		frame.setTitle("Client");
		JButton exitBtn = new JButton("나가기");
		JButton changeBtn = new JButton("문제 변경");
		JButton sendBtn = new JButton("전송");

		category.setHorizontalAlignment(JTextField.CENTER); // text 중앙정렬
		chatLogArea.setEditable(false); // 수정 불가능하게
		rankArea.setEditable(false);
		category.setEditable(false);
		chatLogArea.setLineWrap(true); // 자동 줄바꿈
		rankArea.setLineWrap(true);
		
		frame.setLayout(new BorderLayout());
		
		// 상단 구성
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		JPanel panel1_1 = new JPanel(new BorderLayout());
		JPanel panel1_2 = new JPanel(new BorderLayout(23, 13));
		panel1_1.add(webcamLabel);
		panel1_2.add(BorderLayout.NORTH, category);
		panel1_2.add(BorderLayout.SOUTH, rankArea);

		category.setPreferredSize(new Dimension(100, 40));
		rankArea.setPreferredSize(new Dimension(180, 410));
		
		panel1.add(panel1_1);
		panel1.add(panel1_2);
		
		// 하단 구성
		JPanel panel2 = new JPanel(new BorderLayout(0, 0));
		JPanel panel2_left = new JPanel(new BorderLayout(0, 0));
		JPanel panel2_right = new JPanel(new BorderLayout(0, 0));
		
		JScrollPane scrollArea = new JScrollPane(chatLogArea);
		panel2_left.add(BorderLayout.NORTH, scrollArea);
		
		JPanel panel2_left_south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
		panel2_left_south.add(chatField);
		chatField.setPreferredSize(new Dimension(535, 30));
		panel2_left_south.add(sendBtn);
		sendBtn.setPreferredSize(new Dimension(100, 30));
		panel2_left.add(panel2_left_south);
		
		exitBtn.setPreferredSize(new Dimension(160, 30));
		changeBtn.setPreferredSize(new Dimension(160, 40));
		panel2_right.add(BorderLayout.SOUTH, exitBtn);
		panel2_right.add(BorderLayout.NORTH, changeBtn);

		panel2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 25));

		panel2.add(BorderLayout.WEST, panel2_left);
		panel2.add(BorderLayout.EAST, panel2_right);
		
		frame.add(BorderLayout.CENTER, panel1);
		frame.add(BorderLayout.SOUTH, panel2);
		
		// 프레임 보이기
		frame.setPreferredSize(new Dimension(880, 790));
		frame.pack();
		frame.setResizable(false);
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

		// 엔터키 누르면 전송
		chatField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = chatField.getText();
					if (!message.equals(""))
						send("101|" + userName + "|" + message);
					chatField.setText("");
				}
			}
		});

		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = chatField.getText();
				if (!message.equals(""))
					send("101|" + userName + "|" + message);
				chatField.setText("");
			}
		});
	}

	public static void login() {
		loginFrame = new JFrame();

		loginFrame.setPreferredSize(new Dimension(880, 790));
		loginFrame.setResizable(false);
		loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		loginFrame.setVisible(true);
		loginFrame.setLayout(null);

		JLabel label = new JLabel("닉네임을 입력하세요");
		label.setBounds(365, 285, 200, 100);
		loginFrame.add(label);

		idField = new JTextField();
		loginFrame.add(idField);
		idField.setBounds(350, 360, 150, 30);

		submmitBtn = new JButton("입장");
		submmitBtn.setBounds(350, 400, 150, 30);
		loginFrame.add(submmitBtn);

		loginFrame.pack();

		loginFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				idField.requestFocus();
			}
		});

		submmitBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (idField.getText().equals("") || idField.getText().equals(" "))
					JOptionPane.showMessageDialog(null, "닉네임을 입력하십시오.", "Error", JOptionPane.ERROR_MESSAGE);
				else if(idField.getText().startsWith("방장"))
					JOptionPane.showMessageDialog(null, "방장으로 시작하는 이름은 사용할 수 없습니다.", "Error", JOptionPane.ERROR_MESSAGE);
				else {
					userName = idField.getText();
					loginFrame.setVisible(false);
					frame.setVisible(true);
					receive();
					send("100|" + userName);
				}
			}
		});
	}

	public static void main(String[] args) {

		MainClient c = new MainClient();
		c.startClient("localhost", 55555, 44444);

		login();
	}
}
