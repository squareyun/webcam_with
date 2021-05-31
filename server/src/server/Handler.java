package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

// �� ���� Ŭ���̾�Ʈ�� ����ϵ��� �ϴ� Ŭ����
public class Handler {
	Socket socket;
	Socket msgSocket;
	public ObjectOutputStream dout;
	public OutputStream msgOut;
	
	public static String userName = "";
	
	public Handler(Socket socket, Socket msgSocket) {
		this.socket = socket;
		this.msgSocket = msgSocket;
		sendVideo();
		receive();
	}

	// Ŭ���̾�Ʈ�κ��� �޼����� ����
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
						System.out.println("[�޼��� ���� ����] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						
						String msg = new String(buffer, 0, length, "UTF-8");
						
						String[] msgs = msg.split("\\|");
						switch(msgs[0]) {
						case "100": // ���� �޼���
							userName = msgs[1];
							msg = "**** '" + msgs[1] + "' ���� �����ϼ̽��ϴ�." + "****";
							break;
						}
						
						MainServer.chatLogArea.append(msg + "\n");
						for(Handler user : MainServer.users) {
							user.send(msg);
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						MainServer.users.remove(Handler.this);
						msgSocket.close();
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
	
	// �ش� Ŭ���̾�Ʈ���� �޼����� ����
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					msgOut = msgSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					msgOut.write(buffer);
					msgOut.flush();
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� �۽� ����] "
								+ msgSocket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						MainServer.users.remove(Handler.this);
						msgSocket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
	
	// �ش� Ŭ���̾�Ʈ���� ���� ����
	public void sendVideo() {
		try {
			dout = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("0�� ����");
			e.printStackTrace();
		}
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						dout.writeObject(MainServer.changeIcon);
						dout.flush();
						dout.reset();
					}
				} catch (Exception e) {
					 e.printStackTrace();
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}	
}
