package server;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class MainServer {
	public static ExecutorService threadPool;
	public static Vector<Handler> users = new Vector<Handler>();
	public static Webcam webcam;
	public static JLabel webcamLabel;
	public static JFrame frame;
	public static BufferedImage bm;
	public static ImageIcon im;
	public static Image img;
	public static Image changeImg;
	public static ImageIcon changeIcon;
	
	ServerSocket serverSocket;
	ServerSocket msgServerSocket;

	JTextField chatField;
	JTextField txt3;
	JTextArea rankArea;
	static JTextArea chatLogArea;
	
	public void startServer(int port, int msgPort) {
		try {
			serverSocket = new ServerSocket(port);
			msgServerSocket = new ServerSocket(msgPort);
		} catch (Exception e) {
			e.printStackTrace();
			if (!serverSocket.isClosed())
				stopServer();
			return;
		}
		
		webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.open(true);
		Webcam.getDiscoveryService().setEnabled(false);
		Webcam.getDiscoveryService().stop();
		
		setGui();
		
		// 클라이언트 접속 대기
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						Socket msgSocket = msgServerSocket.accept();
						users.add(new Handler(socket, msgSocket));
						System.out.println("[클라이언트 접속] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
					} catch (Exception e) {
						if (!serverSocket.isClosed())
							stopServer();
						frame.dispose();
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
		
		// 웹캠으로부터 영상 이미지를 캡쳐해서 설정하는 스레드
		Runnable thread2 = new Runnable() {
			@Override
			public void run() {
				while (true) {
					bm = MainServer.webcam.getImage();
					im = new ImageIcon(bm);
					img = im.getImage();
					changeImg = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
					changeIcon = new ImageIcon(changeImg);
					MainServer.webcamLabel.setIcon(changeIcon);
				}
			}
		};
		threadPool.submit(thread2);
	}

	public void stopServer() {
		try {
			frame.dispose();
			Iterator<Handler> it = users.iterator();
			while (it.hasNext()) {
				Handler client = it.next();
				client.socket.close();
				client.msgSocket.close();
				it.remove();
			}

			if (serverSocket != null && !threadPool.isShutdown())
				threadPool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setGui() {
		frame = new JFrame();
		chatField = new JTextField("");
		rankArea = new JTextArea("");
		chatLogArea = new JTextArea("");
		txt3 = new JTextField("");
		webcamLabel = new JLabel();
		frame.setTitle("Server");
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

		chatField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = chatField.getText();
					if (!message.equals("")) {
						for(Handler user : users) {
							user.send("방장>> " + message);
						}
						chatLogArea.append(message + "\n");
					}
					chatField.setText("");
				}
			}
		});

		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = chatField.getText();
				if (!message.equals("")) {
					for(Handler user : users) {
						user.send("방장>> " + message);
					}
					chatLogArea.append(message + "\n");
				}
				chatField.setText("");
			}
		});
		
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		MainServer m = new MainServer();
		m.startServer(55555, 44444);
	}

}
