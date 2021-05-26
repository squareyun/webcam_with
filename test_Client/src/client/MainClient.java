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
		
		//아래 캠 크기 조절을 위해 임시로 frame 사이즈를 1000, 1000으로 늘려둠
		//필요에 따라 수정하여 사용하면 됨
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
			//카메라 캠 크기의 임의 조절을 위한 부분
			//img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			//width와 height으로 원하는 캠 크기 조절, 마지막 인자는 비율에 맞게 화면 비율 변화시켜 주기 위함
			Image img = im.getImage();
			Image changeImg = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
			ImageIcon changeIcon = new ImageIcon(changeImg);
			dout.writeObject(changeIcon);
			l.setIcon(changeIcon);
			dout.flush();
			//문제 해결을 위해 추가된 부분 (다음 한 줄)
			dout.reset();
		}
	}
}
