package client;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class MainClient {
	static Socket socket;

	public static void main(String[] args) throws IOException{
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.open(true);
		
		socket = new Socket("127.0.0.1", 5000);
		
		BufferedImage bm = webcam.getImage();
		
		ObjectOutputStream dout = new ObjectOutputStream(socket.getOutputStream());
		
		ImageIcon im = new ImageIcon(bm);
		
		//�Ʒ� ķ ũ�� ������ ���� �ӽ÷� frame ����� 1000, 1000���� �÷���
		//�ʿ信 ���� �����Ͽ� ����ϸ� ��
		JFrame frame = new JFrame("PC 1");
		frame.setSize(1000, 700);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		
		JLabel l = new JLabel();
		l.setVisible(true);
		
		frame.add(l);
		frame.setVisible(true);
		
		while(true) {
			bm = webcam.getImage();
			im = new ImageIcon(bm);
			//ī�޶� ķ ũ���� ���� ������ ���� �κ�
			//img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			//width�� height���� ���ϴ� ķ ũ�� ����, ������ ���ڴ� ������ �°� ȭ�� ���� ��ȭ���� �ֱ� ����
			Image img = im.getImage();
			Image changeImg = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
			ImageIcon changeIcon = new ImageIcon(changeImg);
			dout.writeObject(changeIcon);
			l.setIcon(changeIcon);
			dout.flush();
			//���� �ذ��� ���� �߰��� �κ� (���� �� ��)
			dout.reset();
		}
	}
}
