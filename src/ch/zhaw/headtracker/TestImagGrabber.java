package ch.zhaw.headtracker;

import java.awt.Graphics2D;
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
					Image background = grabber.getImage().shrink(2);
					
					while (true) {
						Image image = grabber.getImage().shrink(2);
						
						image.subtract(background);
						
						view.update(new ImageView.Painter(image) {
							@Override
							public void draw(Graphics2D graphics) {
							}
						});
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
