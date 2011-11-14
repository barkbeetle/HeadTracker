package ch.zhaw.headtracker;

import ch.zhaw.headtracker.algorithm.Algorithm1;
import ch.zhaw.headtracker.algorithm.AlgorithmRunner;
import ch.zhaw.headtracker.grabber.*;
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
	
	private static void test() {
		ImageGrabber grabber = new FileImageGrabber("res/captures/capture.raw", 752, 480, .1f);

		AlgorithmRunner.runAlgorithm(new Algorithm1(), grabber);
	}
}
