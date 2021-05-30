package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

// 한 명의 클라이언트와 통신하도록 하는 클래스
public class DoubleHandler {
	Socket socket;
	Socket msgSocket;
	public ObjectOutputStream dout;
	public OutputStream msgOut;
	
	public DoubleHandler(Socket socket, Socket msgSocket) {
		this.socket = socket;
		this.msgSocket = msgSocket;
		sendVideo();
		receive();
	}

	// 클라이언트로부터 메세지를 받음
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream msgIn = msgSocket.getInputStream();
						byte[] buffer = new byte[512];
						int length = msgIn.read(buffer);
						if(length == -1) throw new IOException();
						System.out.println("[메세지 수신 성공] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
						String meesage = new String(buffer, 0, length, "UTF-8");
						for(DoubleHandler user : DoubleMainServer.users) {
							user.send(meesage);
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						DoubleMainServer.users.remove(DoubleHandler.this);
						msgSocket.close();
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		DoubleMainServer.threadPool.submit(thread);
	}
	
	// 해당 클라이언트에게 메세지를 전송
	public void send(String meesage) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					msgOut = msgSocket.getOutputStream();
					byte[] buffer = meesage.getBytes("UTF-8");
					msgOut.write(buffer);
					msgOut.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메세지 송신 오류] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						DoubleMainServer.users.remove(DoubleHandler.this);
						msgSocket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		DoubleMainServer.threadPool.submit(thread);
	}
	
	// 해당 클라이언트에게 비디오 전송
	public void sendVideo() {
		try {
			dout = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("0번 에러");
			e.printStackTrace();
		}
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						dout.writeObject(DoubleMainServer.changeIcon);
						dout.flush();
						dout.reset();
					}
				} catch (Exception e) {
					 e.printStackTrace();
				}
			}
		};
		DoubleMainServer.threadPool.submit(thread);
	}	
}
