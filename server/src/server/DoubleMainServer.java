package server;

import java.awt.Image;
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

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class DoubleMainServer {
	public static ExecutorService threadPool;
	public static Vector<DoubleHandler> users = new Vector<DoubleHandler>();
	public static Webcam webcam;
	public static JLabel l;
	public static JFrame frame;
	public static BufferedImage bm;
	public static ImageIcon im;
	public static Image img;
	public static Image changeImg;
	public static ImageIcon changeIcon;
	
	ServerSocket serverSocket;
	ServerSocket msgServerSocket;

	public void startServer(String IP, int port, int msgPort) {
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
		frame = new JFrame("Server");
		frame.setSize(700, 700);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		l = new JLabel();
		l.setVisible(true);
		
		frame.add(l);
		frame.setVisible(true);
		
		// 클라이언트 접속 대기
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						Socket msgSocket = msgServerSocket.accept();
						users.add(new DoubleHandler(socket, msgSocket));
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
					bm = DoubleMainServer.webcam.getImage();
					im = new ImageIcon(bm);
					img = im.getImage();
					changeImg = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
					changeIcon = new ImageIcon(changeImg);
					DoubleMainServer.l.setIcon(changeIcon);
				}
			}
		};
		threadPool.submit(thread2);
	}

	public void stopServer() {
		try {
			frame.dispose();
			Iterator<DoubleHandler> it = users.iterator();
			while (it.hasNext()) {
				DoubleHandler client = it.next();
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

	public static void main(String[] args) {
		DoubleMainServer m = new DoubleMainServer();
		m.startServer("localhost", 55555, 44444);
	}

}
