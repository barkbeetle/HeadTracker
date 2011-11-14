package ch.zhaw.headtracker;

import ch.zhaw.headtracker.algorithm.Algorithm1;
import ch.zhaw.headtracker.algorithm.AlgorithmRunner;
import ch.zhaw.headtracker.grabber.ImageGrabber;
import ch.zhaw.headtracker.grabber.SocketImageGrabber;
import java.io.IOException;

public class SocketInputMain {
	private SocketInputMain() {
	}

	public static void main(String[] args) {
		try {
			test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "IOResourceOpenedButNotSafelyClosed" })
	private static void test() throws IOException {
		ImageGrabber grabber = new SocketImageGrabber("10.0.0.3", (short) 9999, 80000, 752, 480);

		AlgorithmRunner.runAlgorithm(new Algorithm1(), grabber);
	}
}
