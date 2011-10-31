package ch.zhaw.headtracker;

import java.io.IOException;

public class TestImagGrabber {
	private TestImagGrabber() {
	}

	public static void main(String[] args) {
		try {
			test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "SocketOpenedButNotSafelyClosed" })
	private static void test() throws IOException {
		final ImageGrabber grabber = RawInputStreamImageGrabber.fromSocketAddress("10.0.0.3", (short) 9999, 10000, 752, 480);
		final ImageView view = new ImageView(752, 480);
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						view.updateImage(grabber.getImage());
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.setDaemon(true);
		thread.start();
	}
}
