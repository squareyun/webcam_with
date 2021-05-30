package server;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
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
	
	
	TextField chatField;
	TextField rankField;
	TextField txt3;
	static TextArea chatLogArea;
	
	ServerSocket serverSocket;

	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket(port);
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
						users.add(new Handler(socket));
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
		chatField = new TextField("채팅");
		rankField = new TextField("랭킹");
		txt3 = new TextField("");
		chatLogArea = new TextArea("채팅 내역");
		webcamLabel = new JLabel();
		frame.setTitle("Server");
		JButton exitBtn = new JButton("나가기");
		JButton btn2 = new JButton("문제 변경");
		JButton btn3 = new JButton("??");
		JButton sendBtn = new JButton("전송");
		
		rankField.setEditable(false);
		chatLogArea.setEditable(false);
		
		frame.setLayout(null);
		chatLogArea.setBounds(10,500,620,200); //채팅내역
		chatField .setBounds(10,710,520,40); //채팅치는곳
		rankField.setBounds(670,70,200,410); //점수판
		txt3.setBounds(720,20,100,40);
		exitBtn.setBounds(720,700,100,40);
		sendBtn.setBounds(530,710,100,40);
		btn2.setBounds(720,630,100,40);
		btn3.setBounds(720,560,100,40);
		webcamLabel.setSize(640,480);
		
		//프레임에 컴포넌트 추가
		frame.add(webcamLabel);
		frame.add(chatField);
		frame.add(rankField);
		frame.add(txt3);
		frame.add(chatLogArea);
		frame.add(exitBtn);
		frame.add(btn2);
		frame.add(btn3);
		frame.add(sendBtn);
		
		//프레임 보이기
		frame.setPreferredSize(new Dimension(900, 800));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		exitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		MainServer m = new MainServer();
		m.startServer("localhost", 55555);
	}

}
