package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainHandler extends Thread {
	private Socket socket;
	private ArrayList<MainHandler> user_list;
	private ObjectInputStream video_in;
	private ObjectOutputStream video_out;
	private String thread_name;
	
	public MainHandler(Socket socket, ArrayList<MainHandler> user_list) {
		this.user_list = user_list;
		this.socket = socket;
		thread_name = super.getName();
		System.out.println(socket.getInetAddress() + "님이 입장하였습니다.");
		System.out.println("Thread Name : " + thread_name);
		
		
		try {
			video_in = new ObjectInputStream(socket.getInputStream());
			video_out = new ObjectOutputStream(socket.getOutputStream());			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		while (true) {
			int user_size = user_list.size();
		
			for (int i=0; i<user_size; i++) {
				user_list
			}
		}
		
	}

}
