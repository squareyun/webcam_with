package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

// 한 명의 클라이언트와 통신하도록 하는 클래스
public class Handler {
	Socket socket;
	Socket msgSocket;
	
	public ObjectOutputStream dout;
	public OutputStream msgOut;
	
	public String userName = "";
	public int score;
	
	public Handler(Socket socket, Socket msgSocket) {
		this.socket = socket;
		this.msgSocket = msgSocket;
		score = 0;
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
						
						String msg = new String(buffer, 0, length, "UTF-8");
						
						String[] msgs = msg.split("\\|");
						switch(msgs[0]) {
						case "100": // 입장 메세지
							userName = msgs[1];
							score = 0;
							msg = "**** '" + msgs[1] + "' 님이 입장하셨습니다." + "****";
							MainServer.scoreChange("");
							break;
						case "101": // 채팅
							if(MainServer.question.getText().equals(msgs[2])) {
								msg = "##" + msgs[1] + "님 " + "'" + msgs[2] + "' 정답입니다! ##";
								MainServer.changeFlag = true;
							}
							else {
								msg = msgs[1] + ">> " + msgs[2];								
							}
							break;
						}
						
						MainServer.chatLogArea.append(msg + "\n");
						MainServer.chatLogArea.setCaretPosition(MainServer.chatLogArea.getDocument().getLength());
						for(Handler user : MainServer.users) {
							user.send(msg);
						}
						if(MainServer.changeFlag) {
							MainServer.scoreChange(msgs[1]);
							MainServer.changeQuestion();
							MainServer.changeFlag = false;
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류] "
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
	
	// 해당 클라이언트에게 메세지를 전송
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
						System.out.println("[메세지 송신 오류] "
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