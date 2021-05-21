package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.*;

import com.github.sarxos.webcam.Webcam;

public class MainClient {
	static Socket socket;
	
    public static void main(String[] args) throws IOException {
    	
    	Webcam webcam = Webcam.getDefault();
    	webcam.open();
    	
    	socket = new Socket("192.168.219.104", 123);
    	
    	BufferedImage bm = webcam.getImage();
    	
    	ObjectOutputStream dout = new ObjectOutputStream(socket.getOutputStream());
    	
    	ImageIcon im = new ImageIcon(bm);
    	
    	JFrame frame = new JFrame("PC 1");
    	frame.setSize(640, 360);
    	frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
    	
    	JLabel l = new JLabel();
    	l.setVisible(true);
    	
    	frame.add(l);
    	frame.setVisible(true);
    	
    	while (true) {
    		bm = webcam.getImage();
    		im = new ImageIcon(bm);
    		dout.writeObject(im);
    		l.setIcon(im);
    		dout.flush();
    	}
    	
    }

}