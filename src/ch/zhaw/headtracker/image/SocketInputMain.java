package ch.zhaw.headtracker.image;

import ch.zhaw.headtracker.Algorithm;
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
		final ImageGrabber grabber = InputStreamImageGrabber.fromSocketAddress("10.0.0.3", (short) 9999, 40000, 752, 480);
		
		Algorithm.run(grabber);
	}
}
