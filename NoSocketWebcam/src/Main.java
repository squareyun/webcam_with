import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class Main {

	static {
		System.loadLibrary("opencv_java2411");
	}
	
	public static void main(String[] args) {
		VideoCapture cap = new VideoCapture(0);
		
		if(!cap.isOpened()) {
			System.exit(-1);
		}
		
		Mat image = new Mat();
		MyFrame frame = new MyFrame();
		frame.setVisible(true);
		
		while(true) {
			cap.read(image);
			
			if(!image.empty()) {
				frame.render(image);
			} else {
				System.out.println("No captured frame -- camera disconnected");
			}
		}
		

	}

}
