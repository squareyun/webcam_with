package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	
	static String category;
	static int[] before = new int[2];
	JTextField chatField;
	static JTextField question;
	static JTextArea scoreArea;
	static JTextArea chatLogArea;
	static String[][] setOfQuestion = {
			{"운동", "축구", "야구", "배구", "농구", "펜싱", "스키", "테니스", "태권도", "복싱", "달리기"},
			{"동물", "토끼", "사자", "호랑이", "고양이", "말", "하마", "기린", "개", "너구리", "악어"},
			{"영화", "괴물", "7번방의 선물", "국가대표", "범죄도시", "클래식", "부산행", "극한직업", "해운대", "신과함께", "베테랑"}
	};
	static boolean startFlag = true;
	public static boolean changeFlag = false;
	
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
						if(startFlag) {
							changeQuestion();
							startFlag = false;
						}
						
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
		scoreArea = new JTextArea("");
		question = new JTextField("");
		chatLogArea = new JTextArea(11, 1);
		webcamLabel = new JLabel();
		frame.setTitle("Server");
		JButton exitBtn = new JButton("나가기");
		JButton changeBtn = new JButton("문제 변경");
		JButton sendBtn = new JButton("전송");

		question.setHorizontalAlignment(JTextField.CENTER); // text 중앙정렬
		chatLogArea.setEditable(false); // 수정 불가능하게
		scoreArea.setEditable(false);
		question.setEditable(false);
		chatLogArea.setLineWrap(true); // 자동 줄바꿈
		scoreArea.setLineWrap(true);
		
		frame.setLayout(new BorderLayout());
		
		// 상단 구성
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		JPanel panel1_1 = new JPanel(new BorderLayout());
		JPanel panel1_2 = new JPanel(new BorderLayout(23, 13));
		panel1_1.add(webcamLabel);
		panel1_2.add(BorderLayout.NORTH, question);
		panel1_2.add(BorderLayout.SOUTH, scoreArea);

		question.setPreferredSize(new Dimension(100, 40));
		scoreArea.setPreferredSize(new Dimension(180, 410));
		
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
		frame.setPreferredSize(new Dimension(870, 780));
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
						chatLogArea.append("방장>> " + message + "\n");
						chatLogArea.setCaretPosition(chatLogArea.getDocument().getLength());
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
					chatLogArea.append("방장>> " + message + "\n");
					chatLogArea.setCaretPosition(chatLogArea.getDocument().getLength());
				}
				chatField.setText("");
			}
		});
		
		//버튼을 눌러 문제 랜덤 생성
        changeBtn.addActionListener(new ActionListener( ) {
        	public void actionPerformed(ActionEvent e) {
        		changeQuestion();
        	}
        });
        
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public static void changeQuestion() {
		int randomCategory = (int)(Math.random() * 3);
		int randomQuestion = (int)(Math.random() * 10) + 1;
		
		if(before[0] == randomCategory && before[1] == randomQuestion) {
			changeQuestion();
			return;
		}
		
		category = setOfQuestion[randomCategory][0];
		question.setText(setOfQuestion[randomCategory][randomQuestion]);
		
		for(Handler user : users)
			user.send("200|" + category);
		
		before[0] = randomCategory;
		before[1] = randomQuestion;
	}
	
	public static void scoreChange(String correctUser) {
		scoreArea.setText("");
		for(int i=0; i<users.size(); i++) {
			if (users.get(i).userName.equals(correctUser))
				users.get(i).score += 1;
			String msg = users.get(i).userName + ">> " + users.get(i).score + "점\n";
			scoreArea.append(msg);
			
			String temp = "202|";
			if(i == 0)
				temp = "201|";
			for(int j=0; j<users.size(); j++) {
				users.get(j).send(temp + msg);
			}
		}
	}
	
	public static void main(String[] args) {
		MainServer m = new MainServer();
		m.startServer(55555, 44444);
	}

}