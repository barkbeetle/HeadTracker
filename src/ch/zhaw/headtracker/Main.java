package ch.zhaw.headtracker;

import ch.zhaw.headtracker.algorithm.Algorithm1;
import ch.zhaw.headtracker.algorithm.AlgorithmRunner;
import ch.zhaw.headtracker.grabber.FileImageGrabber;
import ch.zhaw.headtracker.grabber.ImageGrabber;
import ch.zhaw.headtracker.grabber.SocketImageGrabber;

public class Main {
	private Main() {
	}

	public static void main(String[] args) {
		try {
			ImageGrabber grabber = new SocketImageGrabber("10.0.0.3", (short) 9999, 752, 480);
			//ImageGrabber grabber = new FileImageGrabber("res/captures/2011-11-14-20-00-06.raw", 752, 480, .1f);

			AlgorithmRunner.runAlgorithm(new Algorithm1(), grabber);
			//	AlgorithmRunner.runAlgorithm(new Algorithm2(), grabber);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
