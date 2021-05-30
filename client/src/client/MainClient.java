package client;

import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainClient {
	Socket socket;
	public static JLabel label;
	public static JFrame frame;
	public static ObjectInputStream in;
	TextField txt1 = new TextField("ä��");
	TextField txt2 = new TextField("");
	TextField txt3 = new TextField("");
	TextArea lbl = new TextArea("ä�� ����");
	
	
	public void startClient(String IP, int port) {
		setGui();
		
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					System.out.println("[���� ���� ����]");
					//receive();
					receiveVideo();
				} catch (Exception e) {
					stopClient();
					System.out.println("[���� ���� ����]");
				}
			}
		};
		thread.start();
	}
	
	public void receiveVideo() {
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			System.out.println("1������");
			e1.printStackTrace();
			stopClient();
		}
		while(true) {
			try {
				label.setIcon((ImageIcon)in.readObject());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				stopClient();
			}
		}
	}

	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				frame.dispose();
				socket.close();
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	public void setGui() {
		label = new JLabel();
		frame = new JFrame();
		frame.setTitle("Client");
		JButton btn1 = new JButton("������");
		JButton btn2 = new JButton("���� ����");
		JButton btn3 = new JButton("??");
		JButton btn4 = new JButton("����");
		frame.setLayout(null);
		lbl.setBounds(10,500,620,200); //ä�ó���
		txt1.setBounds(10,710,520,40); //ä��ġ�°�
		txt2.setBounds(670,70,200,410); //������
		txt3.setBounds(720,20,100,40);
		btn4.setBounds(530,710,100,40);
		btn1.setBounds(720,700,100,40);
		btn2.setBounds(720,630,100,40);
		btn3.setBounds(720,560,100,40);
		//�����ӿ� ������Ʈ �߰�
		frame.add(lbl);
		frame.add(txt1);
		frame.add(txt2);
		frame.add(txt3);
		frame.add(btn4);
		frame.add(btn1);
		frame.add(btn2);
		frame.add(btn3);
		
		//������ ���̱�
		frame.setPreferredSize(new Dimension(900, 800));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String message = txt1.getText();
				send(message);
			}
		});
		
		frame.pack();	
		label = new JLabel();
		label.setSize(640, 480);
		label.setVisible(true);
		
		frame.add(label);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		MainClient c = new MainClient();
		c.startClient("localhost", 55555);
	}
}
