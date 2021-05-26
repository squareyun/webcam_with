package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

public class MainServer {
	private static ServerSocket server;
	private static ArrayList<MainHandler> user_list;

    public static void main(String[] args) {
		try {
			server = new ServerSocket(55555);
			server.setReuseAddress(true); // port를 다시 사용
	    	System.out.println("waiting..");
	    	
	    	user_list = new ArrayList<MainHandler>();
	    	while (true) {
	    		Socket socket = server.accept();
	    		System.out.println("connected..");
	    		MainHandler handler = new MainHandler(socket, user_list);
	    		user_list.add(handler);
	    		System.out.println("Client 1명 입장. 총 " + .size() + "명");
	    		handler.start();
	    	}    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }

}