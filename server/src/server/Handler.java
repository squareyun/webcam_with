package server;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

// 한 명의 클라이언트와 통신하도록 하는 클래스
public class Handler {
	Socket socket;
	BufferedImage bm;
	ImageIcon im;
	Image img;
	Image changeImg;
	ImageIcon changeIcon;
	public ObjectOutputStream dout;
	
	public Handler(Socket socket) {
		this.socket = socket;
		//receive();
		sendVideo();
	}
	
	// 클라이언트로부터 메세지를 받음
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						
						int length = in.read(buffer);
						if(length == -1) throw new IOException();
						System.out.println("[메세지 수신 성공] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
						String meesage = new String(buffer, 0, length, "UTF-8");
						for(Handler user : MainServer.users) {
							user.send(meesage);
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						MainServer.users.remove(Handler.this);
						socket.close();
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
	
	// 해당 클라이언트에게 메세지를 전송
	public void send(String meesage) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = meesage.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메세지 송신 오류] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						MainServer.users.remove(Handler.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
	
	public void sendVideo() {
		try {
			dout = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("0번 에러");
			e.printStackTrace();
		}
		// 클라이언트에게 비디오 전송
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						bm = MainServer.webcam.getImage();
						im = new ImageIcon(bm);
						//카메라 캠 크기의 임의 조절을 위한 부분
						//img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
						//width와 height으로 원하는 캠 크기 조절, 마지막 인자는 비율에 맞게 화면 비율 변화시켜 주기 위함
						img = im.getImage();
						changeImg = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
						changeIcon = new ImageIcon(changeImg);
						broadcast(changeIcon);
						MainServer.l.setIcon(changeIcon);
					}
				} catch (Exception e) {
					 e.printStackTrace();
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
	
	public void broadcast(ImageIcon changeIcon) {
		synchronized (MainServer.users) {
			for (Handler user : MainServer.users) {
				try {
					user.dout.writeObject(changeIcon);
					user.dout.flush();
					user.dout.reset();
				} catch (Exception e) {
					 e.printStackTrace();
					 System.out.println("error this");
				}
			}
		}
	}
	
}
