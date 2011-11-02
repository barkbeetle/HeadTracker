package ch.zhaw.headtracker;

import ch.zhaw.headtracker.image.ImageGrabber;
import ch.zhaw.headtracker.image.InputStreamImageGrabber;
import java.io.FileInputStream;
import java.io.IOException;

public class FileInputMain {
	private FileInputMain() {
	}

	public static void main(String[] args) {
		try {
			test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static void test() throws IOException {
		ImageGrabber grabber = InputStreamImageGrabber.fromFile("res/captures/capture.raw", 752, 480);
		
		Algorithm.run(grabber);
	}
}
