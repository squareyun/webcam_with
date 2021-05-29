package server;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import java.awt.*;
import javax.swing.JButton;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainServer {
	public static ExecutorService threadPool;
	public static Vector<Handler> users = new Vector<Handler>();
	public static Webcam webcam;
	public static JLabel l;
	public static JFrame frame;
	
	TextField txt1 = new TextField("채팅");
	TextField txt2 = new TextField("");
	TextArea lbl = new TextArea("채팅내역");
	TextField txt3 = new TextField("");
	
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
		frame = new JFrame("Server");
		JButton btn4 = new JButton("전송");
		JButton btn1 = new JButton("나가기");
		JButton btn2 = new JButton("문제변경");
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
		frame.setSize(700, 700);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		frame.pack();
		l = new JLabel();
		l.setSize(640,480);
		l.setVisible(true);
		
		frame.add(l);
		frame.setVisible(true);

		// 클라리언트 접속 대기
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

	public static void main(String[] args) {
		MainServer m = new MainServer();
		m.startServer("localhost", 55555);
	}

}
