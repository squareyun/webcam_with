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

// �� ���� Ŭ���̾�Ʈ�� ����ϵ��� �ϴ� Ŭ����
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
	
	// Ŭ���̾�Ʈ�κ��� �޼����� ����
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
						System.out.println("[�޼��� ���� ����] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
						String meesage = new String(buffer, 0, length, "UTF-8");
						for(Handler user : MainServer.users) {
							user.send(meesage);
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����] "
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
	
	// �ش� Ŭ���̾�Ʈ���� �޼����� ����
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
						System.out.println("[�޼��� �۽� ����] "
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
			System.out.println("0�� ����");
			e.printStackTrace();
		}
		// Ŭ���̾�Ʈ���� ���� ����
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						bm = MainServer.webcam.getImage();
						im = new ImageIcon(bm);
						//ī�޶� ķ ũ���� ���� ������ ���� �κ�
						//img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
						//width�� height���� ���ϴ� ķ ũ�� ����, ������ ���ڴ� ������ �°� ȭ�� ���� ��ȭ���� �ֱ� ����
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
