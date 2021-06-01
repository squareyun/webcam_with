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
	
	public String userName = "";
	public int score;
	public static String scoreAreaTemp;
	
	public Handler(Socket socket, Socket msgSocket) {
		this.socket = socket;
		this.msgSocket = msgSocket;
		score = 0;
		sendVideo();
		receive();
	}
	
	public int getScore() {
		return score;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setScore(int score) {
		this.score = score;
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
						
						scoreAreaTemp = "";
						String[] msgs = msg.split("\\|");
						switch(msgs[0]) {
						case "100": // ���� �޼���
							userName = msgs[1];
							score = 0;
							msg = "*************** <" + userName + "> ���� �����ϼ̽��ϴ�." + "***************";
							
							MainServer.updateScore(""); // �����ǿ� �߰�
							scoreAreaTemp = MainServer.scoreArea.getText();
							
							if(MainServer.startFlag) {
								MainServer.changeQuestion();
								MainServer.startFlag = false;
							}
							send("200|" + MainServer.category);
							
							break;
						case "101": // ä��
							if(MainServer.question.getText().equals(msgs[2])) {
								msg = "��" + msgs[1] + "�� " + "" + msgs[2] + " �����Դϴ�! ��";

								MainServer.changeQuestion();	// ���� �ٲٱ�
								MainServer.updateScore(msgs[1]); // ������ �ʱ�ȭ
								scoreAreaTemp = MainServer.scoreArea.getText();
							}
							else { // �Ϲ� �޼���
								msg = msgs[1] + ">> " + msgs[2];								
							}
							break;
						case "300": //Ŭ���̾�Ʈ ����
							userName = msgs[1];
							MainServer.exitedClient(userName);
							MainServer.updateScore("");

							scoreAreaTemp = MainServer.scoreArea.getText();
							
							msg = "*************** <" + userName + "> ���� �����ϼ̽��ϴ�." + "***************";
							send(msg);
							break;
						}
						
						MainServer.chatLogArea.append(msg + "\n");
						MainServer.chatLogArea.setCaretPosition(MainServer.chatLogArea.getDocument().getLength());
						for(Handler user : MainServer.users) {
							user.send(msg); // �޼��� ����
							if(!scoreAreaTemp.equals(""))
								user.send("300|" + scoreAreaTemp); // ������ �ʱ�ȭ �Ѱ� ����
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